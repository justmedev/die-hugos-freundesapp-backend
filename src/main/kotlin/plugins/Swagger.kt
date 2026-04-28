package plugins

import io.github.smiley4.ktoropenapi.OpenApi
import io.github.smiley4.ktoropenapi.openApi
import io.github.smiley4.ktorswaggerui.swaggerUI
import io.ktor.server.application.*
import io.ktor.server.routing.*


fun Application.configureSwagger() {
    install(OpenApi) {
        info {
            title = "Diehugos Backend"
            version = "1.0.0"
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