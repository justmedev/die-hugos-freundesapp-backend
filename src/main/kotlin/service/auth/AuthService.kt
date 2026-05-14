package service.auth

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import core.exceptions.Unauthorized
import de.mkammerer.argon2.Argon2
import domain.commands.CreateUserCommand
import domain.commands.LoginCommand
import domain.commands.RefreshCommand
import domain.commands.RegisterCommand
import domain.models.User
import domain.models.UserTokenPair
import io.ktor.server.config.*
import service.user.UserService
import java.util.*
import org.slf4j.LoggerFactory

class AuthService(
    private val userService: UserService,
    config: ApplicationConfig,
    val argon2: Argon2
) {
    private val logger = LoggerFactory.getLogger(AuthService::class.java)

    val secret = config.property("jwt.secret").getString()
    val issuer = config.property("jwt.issuer").getString()
    val audience = config.property("jwt.audience").getString()
    val accessTokenVerifier: JWTVerifier = JWT.require(Algorithm.HMAC256(secret))
        .withIssuer(issuer)
        .withAudience(audience)
        .build()
    val refreshTokenVerifier: JWTVerifier = JWT.require(Algorithm.HMAC256(secret))
        .withIssuer(issuer)
        .withAudience(audience)
        .withClaim("type", "refresh")
        .build()

    /**
     * Authenticates a user and returns a pair of JWT tokens (access, refresh).
     * If the user is not found or the password is incorrect, returns null.
     */
    suspend fun login(cmd: LoginCommand): UserTokenPair {
        try {
            val user = userService.findByEmail(cmd.email).takeIf {
                argon2.verify(it.password, cmd.password.toCharArray())
            }
            if (user == null) throw Unauthorized()
            return generateTokens(user)
        } catch (e: Exception) {
            if (e !is Unauthorized) {
                logger.error("Error during login for user ${cmd.email}", e)
            }
            throw Unauthorized()
        }
    }

    suspend fun register(cmd: RegisterCommand): User {
        return userService.create(
            CreateUserCommand(
                cmd.email,
                cmd.firstName,
                cmd.lastName,
                cmd.accountHolderName,
                cmd.accountIBAN,
                argon2.hash(2, 2097152, 2, cmd.plaintextPassword.toCharArray()),
                cmd.birthdate,
                cmd.isAdmin
            )
        )
    }

    suspend fun refresh(cmd: RefreshCommand): UserTokenPair {
        try {
            val decoded = refreshTokenVerifier.verify(cmd.refreshToken)
            val userId = decoded.subject.toInt()
            val user = userService.findById(userId)
            return generateTokens(user)
        } catch (e: Exception) {
            logger.warn("Error during token refresh", e)
            throw Unauthorized()
        }
    }

    private fun generateTokens(user: User): UserTokenPair {
        val currentTime = System.currentTimeMillis()

        val accessToken = JWT.create()
            .withAudience(audience)
            .withIssuer(issuer)
            .withSubject(user.id.toString())
            .withClaim("role", if (user.isAdmin) "admin" else "user")
            .withExpiresAt(Date(currentTime + 15 * 60 * 1000)) // 15 min
            .sign(Algorithm.HMAC256(secret))

        val refreshToken = JWT.create()
            .withAudience(audience)
            .withIssuer(issuer)
            .withSubject(user.id.toString())
            .withClaim("type", "refresh")
            .withExpiresAt(Date(currentTime + 7 * 24 * 60 * 60 * 1000)) // 7 days
            .sign(Algorithm.HMAC256(secret))

        return UserTokenPair(accessToken, refreshToken, user)
    }
}