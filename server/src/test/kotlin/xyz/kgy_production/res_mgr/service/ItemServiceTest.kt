package xyz.kgy_production.res_mgr.service

import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import xyz.kgy_production.res_mgr.db.*
import xyz.kgy_production.res_mgr.model.ItemDto
import xyz.kgy_production.res_mgr.model.SearchQuery
import xyz.kgy_production.res_mgr.service.impl.CategoryServiceImpl
import xyz.kgy_production.res_mgr.service.impl.ItemServiceImpl
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ItemServiceTest {

    private lateinit var itemService: ItemService
    private lateinit var categoryService: CategoryService
    private val testDispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setup() {
        // In-memory SQLite for testing
        Database.connect("jdbc:sqlite:file:test?mode=memory&cache=shared", "org.sqlite.JDBC")
        transaction {
            SchemaUtils.create(
                Locations, Tags, Categories, Items, Props,
                ItemTags, ItemLocations, CategoryProps, ItemPropValues
            )
        }
        itemService = ItemServiceImpl(testDispatcher)
        categoryService = CategoryServiceImpl(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        transaction {
            SchemaUtils.drop(
                Locations, Tags, Categories, Items, Props,
                ItemTags, ItemLocations, CategoryProps, ItemPropValues
            )
        }
    }

    @Test
    fun testCreateAndGetItem() = runTest(testDispatcher) {
        val cat = categoryService.createCategory("TestCategory").getOrThrow()
        val item = ItemDto(
            id = "",
            name = "Test Item",
            categoryId = cat.id,
            isSafe = true
        )

        val created = itemService.createItem(item).getOrThrow()
        assertTrue(created.id.isNotEmpty())
        assertEquals("Test Item", created.name)

        val retrieved = itemService.getItem(created.id).getOrThrow()
        assertEquals(created.id, retrieved?.id)
        assertEquals("Test Item", retrieved?.name)
    }

    @Test
    fun testSearchItem() = runTest(testDispatcher) {
        val cat = categoryService.createCategory("Books").getOrThrow()
        val item1 = itemService.createItem(ItemDto("", "Kotlin Book", cat.id, true)).getOrThrow()
        val item2 = itemService.createItem(ItemDto("", "Java Book", cat.id, true)).getOrThrow()

        val result = itemService.getItems(SearchQuery(query = "Kotlin")).getOrThrow()
        assertEquals(1, result.size)
        assertEquals("Kotlin Book", result[0].name)
    }

    @Test
    fun testItemProperties() = runTest(testDispatcher) {
        val cat = categoryService.createCategory("Electronics").getOrThrow()
        val item = ItemDto(
            id = "",
            name = "Laptop",
            categoryId = cat.id,
            isSafe = true,
            props = mapOf("Brand" to "Apple", "Model" to "MacBook Pro")
        )

        val created = itemService.createItem(item).getOrThrow()

        // Fetch to verify props
        val fetched = itemService.getItem(created.id).getOrThrow()
        assertTrue(fetched != null)
        assertEquals("Apple", fetched.props["Brand"])
        assertEquals("MacBook Pro", fetched.props["Model"])
    }
}
