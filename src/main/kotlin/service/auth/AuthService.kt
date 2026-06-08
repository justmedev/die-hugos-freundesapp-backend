package service.auth

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import core.exceptions.Unauthorized
import de.mkammerer.argon2.Argon2
import domain.commands.CreateUserCommand
import domain.models.User
import domain.models.UserTokenPair
import io.ktor.server.config.*
import service.user.UserService
import java.util.*
import org.slf4j.LoggerFactory

class AuthService(
    private val userService: UserService,
    config: ApplicationConfig,
) {

}