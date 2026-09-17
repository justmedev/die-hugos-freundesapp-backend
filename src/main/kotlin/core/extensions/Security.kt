package core.extensions

import core.exceptions.Unauthorized
import domain.contexts.ServiceContext
import domain.models.User
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.plugins.di.*
import service.user.UserService

suspend fun ApplicationCall.requireUserId(): Int {
    val kid = requireKeycloakId()
    return application.dependencies.resolve<UserService>().findByKeycloakId(kid).id
}

suspend fun ApplicationCall.requireUser(): User {
    val kid = requireKeycloakId()
    return application.dependencies.resolve<UserService>().findByKeycloakId(kid)
}

suspend fun ApplicationCall.requireCtx(): ServiceContext {
    return ServiceContext(requireUser())
}

fun ApplicationCall.requireKeycloakId(): String = principal<JWTPrincipal>()?.payload?.subject
    ?: throw Unauthorized()

suspend fun ApplicationCall.requireUser(userService: UserService): domain.models.User {
    val keycloakId = requireKeycloakId()
    return userService.findByKeycloakId(keycloakId)
}