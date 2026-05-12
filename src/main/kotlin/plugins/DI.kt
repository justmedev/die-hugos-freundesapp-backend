package plugins

import de.mkammerer.argon2.Argon2
import de.mkammerer.argon2.Argon2Factory
import io.ktor.server.application.*
import io.ktor.server.config.*
import io.ktor.server.plugins.di.*
import domain.repositories.CashpoolMemberRepository
import domain.repositories.CashpoolMemberRepositoryImpl
import domain.repositories.CashpoolRepository
import domain.repositories.CashpoolRepositoryImpl
import domain.repositories.CashpoolTransactionRepository
import domain.repositories.CashpoolTransactionRepositoryImpl
import domain.repositories.UserRepository
import domain.repositories.UserRepositoryImpl
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
        provide<UserRepository> { UserRepositoryImpl() }
        provide<CashpoolRepository> { CashpoolRepositoryImpl() }
        provide<CashpoolMemberRepository> { CashpoolMemberRepositoryImpl() }
        provide<CashpoolTransactionRepository> { CashpoolTransactionRepositoryImpl() }

        // Services
        provide<UserService> { UserService(resolve()) }
        provide<CashpoolService> { CashpoolService(resolve(), resolve()) }
        provide<CashpoolMemberService> { CashpoolMemberService(resolve(), resolve(), resolve()) }
        provide<CashpoolTransactionService> { CashpoolTransactionService(resolve(), resolve()) }
        provide<CashpoolSettlementService> { CashpoolSettlementService(resolve(), resolve(), resolve()) }
        provide<AuthService> { AuthService(resolve(), resolve(), resolve()) }
    }
}
