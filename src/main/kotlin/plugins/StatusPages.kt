package plugins

import core.exceptions.NotFound
import core.exceptions.NotaCashpoolMember
import core.exceptions.Unauthorized
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.BadRequestException
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*

fun Application.configureStatusPages() {
    install(StatusPages) {
        exception<Throwable> { call, cause ->
            when (cause) {
                is NotFound -> {
                    call.respondText(text = "404: $cause", status = HttpStatusCode.NotFound)
                }

                is BadRequestException -> {
                    call.respondText(text = "400: $cause", status = HttpStatusCode.BadRequest)
                }

                is NotaCashpoolMember -> {
                    call.respondText(text = "403: $cause", status = HttpStatusCode.Forbidden)
                }

                is Unauthorized -> {
                    call.respondText(text = "401: $cause", status = HttpStatusCode.Unauthorized)
                }

                else -> {
                    call.respondText(text = "500: $cause", status = HttpStatusCode.InternalServerError)
                }
            }
        }
    }
}