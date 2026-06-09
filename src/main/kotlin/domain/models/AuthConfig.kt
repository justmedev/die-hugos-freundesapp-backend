package domain.models

import io.ktor.server.config.*

data class AuthConfig(
    val base: String,
    val realm: String,
    val clientId: String,
) {
    val issuerUri get() = "$base/realms/$realm"
    val jwksUri get() = "$base/realms/$realm/protocol/openid-connect/certs"

    companion object {
        fun fromAppConfig(config: ApplicationConfig) = AuthConfig(
            base = config.property("auth.base").getString(),
            realm = config.property("auth.realm").getString(),
            clientId = config.property("auth.clientId").getString(),
        )
    }
}
