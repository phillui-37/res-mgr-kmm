plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.ktor)
    alias(libs.plugins.kotlinSerialization)
    application
}

group = "xyz.kgy_production.res_mgr"
version = "1.0.0"
application {
    mainClass.set("xyz.kgy_production.res_mgr.ApplicationKt")

    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

dependencies {
    implementation(projects.shared)
    implementation(libs.logback)
    implementation(libs.ktor.serverCore)
    implementation(libs.ktor.serverNetty)

    // Ktor Features
    implementation(libs.ktor.server.content.negotiation)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.ktor.serverAuth)

    // Database
    implementation(libs.exposed.core)
    implementation(libs.exposed.jdbc)
    implementation(libs.exposed.dao)
    implementation(libs.exposed.json)
    implementation(libs.exposed.kotlin.datetime)
    implementation(libs.sqlite.driver)

    // DI
    implementation(libs.koin.core)
    implementation(libs.koin.ktor)
    implementation(libs.koin.logger)

    // Utils
    implementation(libs.uuid.creator)

    testImplementation(libs.ktor.serverTestHost)
    testImplementation(libs.kotlin.testJunit)
}