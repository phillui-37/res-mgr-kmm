package xyz.kgy_production.res_mgr.di

import io.ktor.client.HttpClient
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BasicAuthCredentials
import io.ktor.client.plugins.auth.providers.basic
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.dsl.module
import xyz.kgy_production.res_mgr.repo.ResourceRepository
import xyz.kgy_production.res_mgr.repo.ResourceRepositoryImpl
import xyz.kgy_production.res_mgr.viewmodel.ResourceViewModel
import xyz.kgy_production.res_mgr.viewmodel.SettingsViewModel
import xyz.kgy_production.res_mgr.config.AppConfig
import org.koin.core.module.dsl.viewModel

// Since ClientModule is in commonMain, we can't directly instantiate JvmAppConfig if it's jvm-only file.
// Ideally, we move ClientModule to jvmMain for simple apps purely targeting desktop.
// OR we use expect/actual for module or factory.
// For simplicity given the structure, I'll expect config to be provided before this module or use separate platform module.

// Let's assume we pass platform module or define config factory here via expect/actual if needed.
// Simplest way: Define AppConfig platform implementation via expect/actual if strict KMP.
// But user project has JvmAppConfig in jvmMain.
// I will just create a expect function for creating config.

val clientModule = module {
    single<AppConfig> { getPlatformConfig() } // We'll implement this helper in jvmMain

    single {
        HttpClient {
            install(ContentNegotiation) {
                json(Json {
                    prettyPrint = true
                    isLenient = true
                    ignoreUnknownKeys = true
                })
            }

            // Basic Auth with default credentials for now
            install(Auth) {
                basic {
                    credentials {
                        BasicAuthCredentials("admin", "admin")
                    }
                    sendWithoutRequest { true }
                }
            }
        }
    }

    single<ResourceRepository> { ResourceRepositoryImpl(get<HttpClient>(), get<AppConfig>()) }

    viewModel { ResourceViewModel(get()) }
    viewModel { SettingsViewModel(get()) }
}

expect fun getPlatformConfig(): AppConfig
