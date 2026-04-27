package generated_sample

import io.ktor.server.application.*
import io.ktor.server.plugins.defaultheaders.*

fun Application.configureHttp() {
    install(DefaultHeaders) {
        header("X-Engine", "Ktor") // will send this header with each response
    }
}
