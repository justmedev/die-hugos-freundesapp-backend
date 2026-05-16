package plugins

import core.exceptions.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import org.slf4j.LoggerFactory

fun Application.configureStatusPages() {
    val logger = LoggerFactory.getLogger(this::class.java)

    install(StatusPages) {
        exception<Throwable> { call, cause ->
            when (cause.unwrapToDomainException()) {
                is NotFound -> {
                    call.respondText(text = "404: ${cause.message}", status = HttpStatusCode.NotFound)
                }

                is Conflict -> {
                    call.respondText(text = "409: ${cause.message}", status = HttpStatusCode.Conflict)
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

                is Forbidden -> {
                    call.respondText(text = "403: ${cause.message}", status = HttpStatusCode.Forbidden)
                }

                is Unauthorized -> {
                    call.respondText(text = "401: ${cause.message}", status = HttpStatusCode.Unauthorized)
                }

                else -> {
                    call.respondText(text = "500: ${cause.message}", status = HttpStatusCode.InternalServerError)
                }
            }

            logger.warn("A request threw an error! ${cause.message}", cause)
        }
    }
}

private fun Throwable.unwrapToDomainException(): Throwable {
    var current: Throwable? = this
    while (current != null) {
        if (current is NotFound || current is Forbidden || current is Unauthorized || current is Conflict || current is DataQualityException) {
            return current
        }
        current = current.cause
    }
    // If we didn't find a specific domain exception, return the original wrapper
    return this
}