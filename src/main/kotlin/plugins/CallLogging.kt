package plugins

import io.ktor.server.application.*
import io.ktor.server.plugins.calllogging.*
import io.ktor.server.request.httpMethod
import io.ktor.server.request.location

fun Application.configureCallLogging() {
    install(CallLogging) {
        format { call ->
            val status = call.response.status()
            val httpMethod = call.request.httpMethod.value
            val route = call.request.local.uri
            val userAgent = call.request.headers["User-Agent"]
            "$httpMethod $route -> $status, User agent: $userAgent"
        }
    }
}