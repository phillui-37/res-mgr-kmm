package xyz.kgy_production.res_mgr.service.impl

import com.github.f4b6a3.uuid.UuidCreator
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import xyz.kgy_production.res_mgr.db.*
import xyz.kgy_production.res_mgr.model.*
import xyz.kgy_production.res_mgr.service.ItemService
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.util.zip.GZIPInputStream
import java.nio.charset.StandardCharsets
import java.util.UUID

import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq

class ItemServiceImpl(
    private val ioDispatcher: CoroutineDispatcher
) : ItemService {

    private suspend fun <T> dbQuery(block: () -> T): Result<T> = withContext(ioDispatcher) {
        try {
            Result.success(transaction { block() })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getItems(query: SearchQuery): Result<List<ItemDto>> = dbQuery {
        val qStr = query.query.trim()
        val catIdStr = query.filter["categoryId"]

        var baseQuery = Items.selectAll()

        if (qStr.isNotEmpty()) {
            baseQuery = baseQuery.where { Items.name like "%$qStr%" }
        }

        if (!catIdStr.isNullOrBlank()) {
            try {
                val catUuid = UUID.fromString(catIdStr)
                baseQuery = baseQuery.andWhere { Items.categoryId eq catUuid }
            } catch (e: Exception) {
                // ignore invalid category UUID filter
            }
        }

        val itemRows = baseQuery
            .limit(query.limit, query.offset.toLong())
            .toList()

        val itemIds = itemRows.map { it[Items.id] }

        // Load tags
        val tagsMap = ItemTags.innerJoin(Tags)
            .select(ItemTags.itemId, Tags.id, Tags.name)
            .where { ItemTags.itemId inList itemIds }
            .groupBy { it[ItemTags.itemId] }
            .mapValues { entry ->
                entry.value.map { TagDto(it[Tags.id].toString(), it[Tags.name]) }
            }

        // Load locations
        val locsMap = ItemLocations.innerJoin(Locations)
            .select(ItemLocations.itemId, Locations.id, Locations.name, Locations.path, Locations.clientType)
            .where { ItemLocations.itemId inList itemIds }
            .groupBy { it[ItemLocations.itemId] }
            .mapValues { entry ->
                entry.value.map {
                    LocationDto(
                        it[Locations.id].toString(),
                        it[Locations.name],
                        it[Locations.path],
                        it[Locations.clientType]
                    )
                }
            }

        itemRows.map { row ->
            val uuid = row[Items.id]
            ItemDto(
                id = uuid.toString(),
                name = row[Items.name],
                categoryId = row[Items.categoryId].toString(),
                isSafe = row[Items.isSafe],
                tags = tagsMap[uuid] ?: emptyList(),
                locations = locsMap[uuid] ?: emptyList()
            )
        }
    }

    override suspend fun getItem(id: String): Result<ItemDto?> = dbQuery {
        val uuid = try { UUID.fromString(id) } catch(e: Exception) { null }
        if (uuid == null) return@dbQuery null

        Items.selectAll().where { Items.id eq uuid }
            .map {
                ItemDto(
                    id = it[Items.id].toString(),
                    name = it[Items.name],
                    categoryId = it[Items.categoryId].toString(),
                    isSafe = it[Items.isSafe]
                )
            }.singleOrNull()
    }

    override suspend fun createItem(item: ItemDto): Result<ItemDto> = dbQuery {
        val newId = UuidCreator.getTimeOrderedEpoch()

        Items.insert {
            it[id] = newId
            it[name] = item.name
            it[categoryId] = UUID.fromString(item.categoryId)
            it[isSafe] = item.isSafe
        }

        // Save Tags
        if (item.tags.isNotEmpty()) {
            // Simple logic: If tag ID provided, link it. If only name, finding existing or create.
            // For now assuming existing tags linking or new tag creation by name check?
            // Let's implement simple linking by ID if present, or create new tag if name present and ID empty.

            item.tags.forEach { tagDto ->
                var tagId: UUID? = null
                if (tagDto.id.isNotEmpty()) {
                    tagId = try { UUID.fromString(tagDto.id) } catch(_: Exception) { null }
                    // Check existence?
                }

                if (tagId == null && tagDto.name.isNotEmpty()) {
                    // Try find by name
                    val existing = Tags.selectAll().where { Tags.name eq tagDto.name }.singleOrNull()
                    if (existing != null) {
                        tagId = existing[Tags.id]
                    } else {
                        // Create new
                        val newTagId = UuidCreator.getTimeOrderedEpoch()
                        Tags.insert {
                            it[id] = newTagId
                            it[name] = tagDto.name
                        }
                        tagId = newTagId
                    }
                }

                if (tagId != null) {
                   ItemTags.insert {
                       it[itemId] = newId
                       it[this.tagId] = tagId!!
                   }
                }
            }
        }

        // Save Props
        if (item.props.isNotEmpty()) {
            item.props.forEach { (key, value) ->
                // Check Prop exists
                var propId = Props.selectAll()
                    .where { Props.name eq key }
                    .limit(1)
                    .map { it[Props.id] }
                    .singleOrNull()

                if (propId == null) {
                    val newPropId = UuidCreator.getTimeOrderedEpoch()
                    Props.insert {
                        it[id] = newPropId
                        it[name] = key
                        it[type] = "string" // default
                    }
                    propId = newPropId
                }

                ItemPropValues.insert {
                    it[itemId] = newId
                    it[this.propId] = propId!!
                    it[this.value] = value
                }
            }
        }

        // Basic impl: ignoring tags/props for brevity in this step, but ID is returned
        item.copy(id = newId.toString())
    }

    override suspend fun updateItem(item: ItemDto): Result<ItemDto> = dbQuery {
        val uuid = UUID.fromString(item.id)
        Items.update({ Items.id eq uuid }) {
            it[name] = item.name
            it[categoryId] = UUID.fromString(item.categoryId)
            it[isSafe] = item.isSafe
        }
        item
    }

    override suspend fun deleteItem(id: String): Result<Boolean> = dbQuery {
        val uuid = try { UUID.fromString(id) } catch(e: Exception) { return@dbQuery false }
        val count = Items.deleteWhere { Items.id eq uuid }
        count > 0
    }

    override suspend fun importBatch(content: ByteArray): Result<Int> = withContext(ioDispatcher) {
        val itemsToInsert = try {
            val gzipStream = GZIPInputStream(ByteArrayInputStream(content))
            val reader = gzipStream.bufferedReader(StandardCharsets.UTF_8)
            val lines = reader.readLines()

            // Simple parsing: ID, CategoryID, IsSafe, Name... (Assume CSV)
            // Skip header
            lines.drop(1).mapNotNull { line ->
                 val parts = line.split(",")
                 if (parts.size >= 4) {
                     // Determine ID (new or existing)
                     // Here we assume creating new items from batch mainly
                     val catId = parts[1].trim() // Assume valid UUID string for now
                     val safe = parts[2].trim().toBoolean()
                     val name = parts[3].trim()
                     ItemDto(id = "", categoryId = catId, isSafe = safe, name = name)
                 } else null
            }
        } catch (e: Exception) {
            return@withContext Result.failure<Int>(e)
        }

        if (itemsToInsert.isEmpty()) return@withContext Result.success(0)

        dbQuery {
            // fast implementation using batch insert if supported or simple loop
            var count = 0
            itemsToInsert.forEach { item ->
                 val newId = UuidCreator.getTimeOrderedEpoch()
                 try {
                     Items.insert {
                         it[id] = newId
                         it[name] = item.name
                         it[categoryId] = UUID.fromString(item.categoryId)
                         it[isSafe] = item.isSafe
                     }
                     count++
                 } catch (e: Exception) {
                     // ignore failed rows or log?
                 }
            }
            count
        }
    }
}
