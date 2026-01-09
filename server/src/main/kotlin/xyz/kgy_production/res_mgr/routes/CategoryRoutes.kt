package xyz.kgy_production.res_mgr.routes

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject
import xyz.kgy_production.res_mgr.model.CategoryDto
import xyz.kgy_production.res_mgr.service.CategoryService

fun Route.categoryRoutes() {
    val service by inject<CategoryService>()

    route("/categories") {
        get {
            service.getCategories().fold(
                onSuccess = { call.respond(it) },
                onFailure = { call.respondText(it.message ?: "Error", status = HttpStatusCode.InternalServerError) }
            )
        }
        post {
            try {
                // simple body with name for now, or DTO
                val dto = call.receive<CategoryDto>()
                service.createCategory(dto.name).fold(
                    onSuccess = { call.respond(it) },
                    onFailure = { call.respondText(it.message ?: "Error", status = HttpStatusCode.InternalServerError) }
                )
            } catch (e: Exception) {
                call.respondText("Invalid: ${e.message}", status = HttpStatusCode.BadRequest)
            }
        }
    }
}

