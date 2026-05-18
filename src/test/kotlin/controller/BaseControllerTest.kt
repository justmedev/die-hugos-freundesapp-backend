package controller

import com.auth0.jwt.interfaces.Payload
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.plugins.di.*
import io.ktor.server.testing.*
import io.mockk.every
import io.mockk.mockk
import plugins.configureResources
import plugins.configureSSE
import plugins.configureSerialization
import plugins.configureStatusPages
import service.auth.AuthService
import service.cashpool.CashpoolService
import service.cashpool_member.CashpoolMemberService
import service.cashpool_settlement.CashpoolSettlementService
import service.cashpool_settlement.CashpoolSuggestedSettlementCalculationService
import service.cashpool_transaction.CashpoolTransactionService
import service.user.UserService

abstract class BaseControllerTest {
    val userService = mockk<UserService>(relaxed = true)
    val cashpoolService = mockk<CashpoolService>(relaxed = true)
    val cashpoolMemberService = mockk<CashpoolMemberService>(relaxed = true)
    val cashpoolTransactionService = mockk<CashpoolTransactionService>(relaxed = true)
    val cashpoolSettlementService = mockk<CashpoolSettlementService>(relaxed = true)
    val cashpoolSuggestedSettlementCalculationService = mockk<CashpoolSuggestedSettlementCalculationService>(relaxed = true)
    val authService = mockk<AuthService>(relaxed = true)

    fun createMockPrincipal(userId: Int, role: String = "user"): JWTPrincipal {
        val payload = mockk<Payload> {
            every { subject } returns userId.toString()
            every { claims } returns mapOf("role" to mockk {
                every { asString() } returns role
            })
        }
        return JWTPrincipal(payload)
    }

    fun withTestApplication(
        principal: JWTPrincipal? = null,
        block: suspend ApplicationTestBuilder.() -> Unit
    ) = testApplication {
        application {
            configureSerialization()
            configureResources()
            configureSSE()
            configureStatusPages()
            dependencies {
                provide { userService }
                provide { cashpoolService }
                provide { cashpoolMemberService }
                provide { cashpoolTransactionService }
                provide { cashpoolSettlementService }
                provide { cashpoolSuggestedSettlementCalculationService }
                provide { authService }
            }
            authentication {
                bearer {
                    authenticate {
                        principal
                    }
                }
            }
            configureControllers()
        }
        block()
    }

    fun ApplicationTestBuilder.createClient() = createClient {
        install(ContentNegotiation) {
            json()
        }
    }
}
