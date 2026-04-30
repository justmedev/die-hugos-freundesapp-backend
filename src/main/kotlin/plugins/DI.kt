package plugins

import de.mkammerer.argon2.Argon2
import de.mkammerer.argon2.Argon2Factory
import io.ktor.server.application.*
import io.ktor.server.config.*
import io.ktor.server.plugins.di.*
import service.auth.AuthService
import service.cashpool_member.CashpoolMemberService
import service.cashpool_transaction.CashpoolTransactionService
import service.cashpool.CashpoolService
import service.user.UserService

fun Application.configureDependencyInjection() {
    val env = environment
    dependencies {
        provide<ApplicationConfig> { env.config }
        provide<Argon2> { Argon2Factory.create() }

        // Services
        provide<UserService> { UserService() }
        provide<CashpoolService> { CashpoolService(resolve()) }
        provide<CashpoolMemberService> { CashpoolMemberService(resolve(), resolve()) }
        provide<CashpoolTransactionService> { CashpoolTransactionService(resolve(), resolve(), resolve()) }
        provide<AuthService> { AuthService(resolve(), resolve(), resolve()) }
    }
}
