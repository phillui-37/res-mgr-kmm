package xyz.kgy_production.res_mgr.service.impl

import com.github.f4b6a3.uuid.UuidCreator
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import xyz.kgy_production.res_mgr.db.Categories
import xyz.kgy_production.res_mgr.model.CategoryDto
import xyz.kgy_production.res_mgr.service.CategoryService
import java.util.UUID

class CategoryServiceImpl(
    private val ioDispatcher: CoroutineDispatcher
) : CategoryService {

    private suspend fun <T> dbQuery(block: () -> T): Result<T> = withContext(ioDispatcher) {
        try {
            Result.success(transaction { block() })
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getCategories(): Result<List<CategoryDto>> = dbQuery {
        Categories.selectAll().map {
            CategoryDto(id = it[Categories.id].toString(), name = it[Categories.name])
        }
    }

    override suspend fun createCategory(name: String): Result<CategoryDto> = dbQuery {
        val newId = UuidCreator.getTimeOrderedEpoch()
        Categories.insert {
            it[id] = newId
            it[Categories.name] = name
        }
        CategoryDto(id = newId.toString(), name = name)
    }

    override suspend fun deleteCategory(id: String): Result<Boolean> = dbQuery {
        val uuid = try { UUID.fromString(id) } catch (e: Exception) { return@dbQuery false }
        Categories.deleteWhere { Categories.id eq uuid } > 0
    }
}

