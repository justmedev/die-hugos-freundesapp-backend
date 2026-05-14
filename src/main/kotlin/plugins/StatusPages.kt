package plugins

import core.exceptions.DataQualityException
import core.exceptions.NotFound
import core.exceptions.NotaCashpoolMember
import core.exceptions.Unauthorized
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*

fun Application.configureStatusPages() {
    install(StatusPages) {
        exception<Throwable> { call, cause ->
            when (cause.unwrapToDomainException()) {
                is NotFound -> {
                    call.respondText(text = "404: ${cause.message}", status = HttpStatusCode.NotFound)
                }

                is IllegalArgumentException -> {
                    call.respondText(text = "400: ${cause.message}", status = HttpStatusCode.BadRequest)
                }

                is DataQualityException -> {
                    call.respondText(text = "422: ${cause.message}", status = HttpStatusCode.UnprocessableEntity)
                }

                is BadRequestException -> {
                    call.respondText(text = "400: ${cause.message}", status = HttpStatusCode.BadRequest)
                }

                is NotaCashpoolMember -> {
                    call.respondText(text = "403: ${cause.message}", status = HttpStatusCode.Forbidden)
                }

                is Unauthorized -> {
                    call.respondText(text = "401: ${cause.message}", status = HttpStatusCode.Unauthorized)
                }

                else -> {
                    call.respondText(text = "500: ${cause.message}", status = HttpStatusCode.InternalServerError)
                }
            }
        }
    }
}

private fun Throwable.unwrapToDomainException(): Throwable {
    var current: Throwable? = this
    while (current != null) {
        if (current is DataQualityException) {
            return current
        }
        current = current.cause
    }
    // If we didn't find a specific domain exception, return the original wrapper
    return this
}