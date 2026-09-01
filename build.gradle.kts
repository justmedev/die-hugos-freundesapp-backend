import org.jetbrains.exposed.v1.plugin.core.migration.VersionFormat
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.versionCatalogeUpdate)
    alias(libs.plugins.kotlin.jvm)
    alias(ktorLibs.plugins.ktor)
    alias(libs.plugins.exposed)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.dotenv)
}

group = "at.ilja_busch"
version = "1.0.0-SNAPSHOT"

application {
    mainClass = "io.ktor.server.netty.EngineMain"
}

kotlin {
    jvmToolchain(25)
}

exposed {
    migrations {
        tablesPackage.set("domain.tables")
        databaseUrl.set(env.POSTGRES_URL.value)
        databaseUser.set(env.POSTGRES_USER.value)
        databasePassword.set(env.POSTGRES_PASSWORD.value)
        fileVersionFormat = VersionFormat.MAJOR_TIMESTAMP
    }
}

dependencies {
    implementation(ktorLibs.serialization.kotlinx.json)
    implementation(ktorLibs.server.auth)
    implementation(ktorLibs.server.auth.jwt)
    implementation(ktorLibs.server.callLogging)
    implementation(ktorLibs.server.contentNegotiation)
    implementation(ktorLibs.server.core)
    implementation(ktorLibs.server.defaultHeaders)
    implementation(ktorLibs.server.di)
    implementation(ktorLibs.server.netty)
    implementation(ktorLibs.server.requestValidation)
    implementation(ktorLibs.server.resources)
    implementation(ktorLibs.server.statusPages)
    implementation(ktorLibs.server.sse)
    implementation(ktorLibs.server.cors)

    implementation(libs.exposed.core)
    implementation(libs.exposed.jdbc)
    implementation(libs.exposed.dao)
    implementation(libs.exposed.kotlin.datetime)
    implementation(libs.h2database.h2)
    implementation(libs.hayden.khealth)
    implementation(libs.logback.classic)
    implementation(libs.postgresql)
    implementation(libs.argon2)
    implementation(libs.ktor.openapi)
    implementation(libs.ktor.swagger)
    implementation(libs.konform)

    implementation(libs.schema.kenerator.core)
    implementation(libs.schema.kenerator.serialization)
    implementation(libs.schema.kenerator.swagger)

    testImplementation(kotlin("test"))
    testImplementation(ktorLibs.server.testHost)
    testImplementation(ktorLibs.client.contentNegotiation)
    testImplementation(ktorLibs.client.serialization)
    testImplementation(libs.mockk)
}