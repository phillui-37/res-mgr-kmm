package xyz.kgy_production.res_mgr.repo

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.forms.formData
import io.ktor.client.request.forms.submitFormWithBinaryData
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import xyz.kgy_production.res_mgr.model.CategoryDto
import xyz.kgy_production.res_mgr.model.ItemDto
import xyz.kgy_production.res_mgr.model.SearchQuery
import xyz.kgy_production.res_mgr.config.AppConfig

interface ResourceRepository {
    suspend fun getCategories(): Result<List<CategoryDto>>
    suspend fun getItems(query: SearchQuery): Result<List<ItemDto>>
    suspend fun createItem(item: ItemDto): Result<ItemDto>
    suspend fun deleteItem(id: String): Result<Boolean>
    suspend fun importBatch(content: ByteArray): Result<String>
}

class ResourceRepositoryImpl(
    private val client: HttpClient,
    private val config: AppConfig
) : ResourceRepository {

    private val baseUrl: String
        get() = config.serverUrl

    override suspend fun getCategories(): Result<List<CategoryDto>> = runCatching {
        client.get("$baseUrl/categories").body()
    }

    override suspend fun getItems(query: SearchQuery): Result<List<ItemDto>> = runCatching {
        client.get("$baseUrl/items") {
            parameter("q", query.query)
            parameter("limit", query.limit)
            parameter("offset", query.offset)
            // Flatten filter map if needed, or send as complex param?
            // Simple param for now
        }.body()
    }

    override suspend fun createItem(item: ItemDto): Result<ItemDto> = runCatching {
        client.post("$baseUrl/items") {
            contentType(ContentType.Application.Json)
            setBody(item)
        }.body()
    }

    override suspend fun deleteItem(id: String): Result<Boolean> = runCatching {
        val response: HttpResponse = client.delete("$baseUrl/items/$id")
        response.status.value in 200..299
    }

    override suspend fun importBatch(content: ByteArray): Result<String> = runCatching {
        val response: HttpResponse = client.submitFormWithBinaryData(
            url = "$baseUrl/items/batch",
            formData = formData {
                append("file", content, Headers.build {
                    append(HttpHeaders.ContentType, "application/gzip")
                    append(HttpHeaders.ContentDisposition, "filename=\"batch.csv.gz\"")
                })
            }
        )
        response.body()
    }
}
