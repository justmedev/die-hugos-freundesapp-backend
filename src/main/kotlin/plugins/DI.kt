package plugins

import de.mkammerer.argon2.Argon2
import de.mkammerer.argon2.Argon2Factory
import io.ktor.server.application.*
import io.ktor.server.config.ApplicationConfig
import io.ktor.server.plugins.di.*
import service.auth.AuthService
import service.users.UsersService

fun Application.configureDependencyInjection() {
    val env = environment
    dependencies {
        provide<ApplicationConfig> { env.config }

        provide<Argon2> { Argon2Factory.create() }
        provide<UsersService> { UsersService() }
        provide<AuthService> { AuthService(resolve(), resolve(), resolve()) }
    }
}
