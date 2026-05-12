package plugins

import de.mkammerer.argon2.Argon2
import de.mkammerer.argon2.Argon2Factory
import io.ktor.server.application.*
import io.ktor.server.config.*
import io.ktor.server.plugins.di.*
import repositories.CashpoolRepository
import repositories.CashpoolRepositoryImpl
import repositories.CashpoolTransactionRepository
import repositories.CashpoolTransactionRepositoryImpl
import service.auth.AuthService
import service.cashpool.CashpoolService
import service.cashpool_member.CashpoolMemberService
import service.cashpool_settlement.CashpoolSettlementService
import service.cashpool_transaction.CashpoolTransactionService
import service.user.UserService

fun Application.configureDependencyInjection() {
    val env = environment
    dependencies {
        provide<ApplicationConfig> { env.config }
        provide<Argon2> { Argon2Factory.create() }

        // Repositories
        provide<CashpoolRepository> { CashpoolRepositoryImpl() }
        provide<CashpoolTransactionRepository> { CashpoolTransactionRepositoryImpl() }

        // Services
        provide<UserService> { UserService() }
        provide<CashpoolService> { CashpoolService(resolve()) }
        provide<CashpoolMemberService> { CashpoolMemberService(resolve(), resolve()) }
        provide<CashpoolTransactionService> { CashpoolTransactionService(resolve(), resolve()) }
        provide<CashpoolSettlementService> { CashpoolSettlementService(resolve(), resolve(), resolve()) }
        provide<AuthService> { AuthService(resolve(), resolve(), resolve()) }
    }
}
