@file:OptIn(ExperimentalSerializationApi::class)

package plugins


import io.github.smiley4.ktoropenapi.OpenApi
import io.github.smiley4.ktoropenapi.config.SchemaGenerator
import io.github.smiley4.ktoropenapi.openApi
import io.github.smiley4.ktorswaggerui.swaggerUI
import io.ktor.server.application.*
import io.ktor.server.routing.*
import kotlinx.serialization.ExperimentalSerializationApi


fun Application.configureSwagger() {
    install(OpenApi) {
        info {
            title = "Diehugos Backend"
            version = "1.0.0"
        }

        schemas {
            generator = SchemaGenerator.kotlinx()
        }
    }

    routing {
        route("api.json") {
            openApi()
        }
        route("swagger") {
            swaggerUI("/api.json") {

            }
        }
    }
}