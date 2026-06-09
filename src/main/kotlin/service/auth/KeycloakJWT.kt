package service.auth

import io.ktor.server.auth.jwt.*

data class KeycloakJWT(
    val keycloakId: String,
    val email: String,
    val firstName: String,
    val lastName: String,
    val isEmailVerified: Boolean,
    val username: String
) {
    companion object {
        fun from(credential: JWTCredential) = KeycloakJWT(
            credential.subject!!,
            credential.payload.getClaim("email").asString(),
            credential.payload.getClaim("given_name").asString(),
            credential.payload.getClaim("family_name").asString(),
            credential.payload.getClaim("email_verified").asBoolean(),
            credential.payload.getClaim("preferred_username").asString()
        )
    }
}