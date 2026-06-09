package core.extensions

import core.exceptions.Unauthorized
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*

fun ApplicationCall.requireUserId(): Int = principal<JWTPrincipal>()?.payload?.subject?.toIntOrNull()
    ?: throw Unauthorized()

fun ApplicationCall.requireKeycloakId(): String = principal<JWTPrincipal>()?.payload?.subject
    ?: throw Unauthorized()

fun ApplicationCall.requireUserRole(): String = principal<JWTPrincipal>()?.payload?.claims?.get("role")?.asString()
    ?: throw Unauthorized()

suspend fun ApplicationCall.requireUser(userService: service.user.UserService): domain.models.User {
    val keycloakId = requireKeycloakId()
    return userService.findByKeycloakId(keycloakId)
}