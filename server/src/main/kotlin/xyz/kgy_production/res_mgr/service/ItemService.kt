package xyz.kgy_production.res_mgr.service

import xyz.kgy_production.res_mgr.model.ItemDto
import xyz.kgy_production.res_mgr.model.SearchQuery

interface ItemService {
    suspend fun getItems(query: SearchQuery): Result<List<ItemDto>>
    suspend fun getItem(id: String): Result<ItemDto?>
    suspend fun createItem(item: ItemDto): Result<ItemDto>
    suspend fun updateItem(item: ItemDto): Result<ItemDto>
    suspend fun deleteItem(id: String): Result<Boolean>
    suspend fun importBatch(content: ByteArray): Result<Int>
}

