package xyz.kgy_production.res_mgr.routes

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.http.content.PartData
import io.ktor.http.content.forEachPart
import io.ktor.utils.io.core.readBytes
import io.ktor.utils.io.readRemaining
import kotlinx.io.readByteArray
import org.koin.ktor.ext.inject
import xyz.kgy_production.res_mgr.model.ItemDto
import xyz.kgy_production.res_mgr.model.SearchQuery
import xyz.kgy_production.res_mgr.service.ItemService

fun Route.itemRoutes() {
    val service by inject<ItemService>()

    route("/items") {
        get {
            val queryStr = call.request.queryParameters["q"] ?: ""
            val limit = call.request.queryParameters["limit"]?.toIntOrNull() ?: 20
            val offset = call.request.queryParameters["offset"]?.toIntOrNull() ?: 0

            val query = SearchQuery(query = queryStr, limit = limit, offset = offset)

            service.getItems(query).fold(
                onSuccess = { call.respond(it) },
                onFailure = {
                    call.application.environment.log.error("Failed to get items", it)
                    call.respondText(it.message ?: "Error", status = HttpStatusCode.InternalServerError)
                }
            )
        }

        post {
            try {
                val item = call.receive<ItemDto>()
                service.createItem(item).fold(
                    onSuccess = { call.respond(it) },
                    onFailure = {
                        call.application.environment.log.error("Failed to create item", it)
                        call.respondText(it.message ?: "Error", status = HttpStatusCode.InternalServerError)
                    }
                )
            } catch (e: Exception) {
                call.respondText("Invalid format: ${e.message}", status = HttpStatusCode.BadRequest)
            }
        }

        get("/{id}") {
            val id = call.parameters["id"] ?: return@get call.respondText("Missing ID", status = HttpStatusCode.BadRequest)
            service.getItem(id).fold(
                onSuccess = {
                    if (it != null) call.respond(it) else call.respondText("Not Found", status = HttpStatusCode.NotFound)
                },
                onFailure = { call.respondText(it.message ?: "Error", status = HttpStatusCode.InternalServerError) }
            )
        }

        delete("/{id}") {
            val id = call.parameters["id"] ?: return@delete call.respondText("Missing ID", status = HttpStatusCode.BadRequest)
            service.deleteItem(id).fold(
                onSuccess = { call.respond(HttpStatusCode.OK) },
                onFailure = { call.respondText(it.message ?: "Error", status = HttpStatusCode.InternalServerError) }
            )
        }

        post("/batch") {
            val multipart = call.receiveMultipart()
            var count = 0
            multipart.forEachPart { part ->
                if (part is PartData.FileItem) {
                    val fileBytes = part.provider().readRemaining().readByteArray()
                    service.importBatch(fileBytes).fold(
                        onSuccess = { count += it },
                        onFailure = {
                            call.application.environment.log.error("Batch import failed", it)
                        }
                    )
                }
                part.dispose()
            }
            call.respondText("Imported $count items")
        }
    }
}
