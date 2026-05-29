package plugins

import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.server.application.*
import io.ktor.server.plugins.cors.routing.CORS

fun Application.configureCORS() {3
    install(CORS) {
        allowMethod(HttpMethod.Options)
        allowMethod(HttpMethod.Put)
        allowMethod(HttpMethod.Delete)
        allowMethod(HttpMethod.Patch)
        allowHeader(HttpHeaders.Authorization)
        allowHeader(HttpHeaders.ContentType)
        allowHeader(HttpHeaders.Accept)
        allowHeader("X-Requested-With")
        allowHeadersPrefixed("Sec-")
        allowHeader("Host")
        allowHeader("Referer")
        allowHeader("DNT")
        allowHeader("Priority")
        allowHeader("Accept-Encoding")
        allowHeader("User-Agent")
        allowHeader("Origin")
        anyHost() // @TODO: Don't do this in production if possible. Try to limit it.
    }
}