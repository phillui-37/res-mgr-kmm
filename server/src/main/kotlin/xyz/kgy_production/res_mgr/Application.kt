package xyz.kgy_production.res_mgr

import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.auth.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.koin.ktor.plugin.Koin
import xyz.kgy_production.res_mgr.db.*
import xyz.kgy_production.res_mgr.di.serverModule
import xyz.kgy_production.res_mgr.routes.itemRoutes
import xyz.kgy_production.res_mgr.routes.categoryRoutes
import java.io.File

fun main() {
    embeddedServer(Netty, port = SERVER_PORT, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    // Logging setup - ensure logs dir exists
    val logsDir = File("logs")
    if (!logsDir.exists()) {
        logsDir.mkdirs()
    }

    install(Koin) {
        // Simple print logger for now to avoid dependency issues with slf4j logger imports
        // or just default
        modules(serverModule)
    }

    install(ContentNegotiation) {
        json(Json {
            prettyPrint = true
            isLenient = true
            ignoreUnknownKeys = true
        })
    }

    install(Authentication) {
        basic("auth-basic") {
            realm = "Access to Resource Manager"
            validate { credentials ->
                if (credentials.name == "admin" && credentials.password == "admin") {
                    UserIdPrincipal(credentials.name)
                } else {
                    null
                }
            }
        }
    }

    // Database
    val dataDir = File("data")
    if (!dataDir.exists()) {
        dataDir.mkdirs()
    }
    val dbFile = File(dataDir, "data.sqlite")
    Database.connect("jdbc:sqlite:${dbFile.absolutePath}", "org.sqlite.JDBC")

    transaction {
        SchemaUtils.create(
            Locations, Tags, Categories, Items, Props,
            ItemTags, ItemLocations, CategoryProps, ItemPropValues
        )
    }

    routing {
        get("/") {
            call.respondText("Ktor: ${Greeting().greet()}")
        }
        authenticate("auth-basic") {
            itemRoutes()
            categoryRoutes()
        }
    }
}