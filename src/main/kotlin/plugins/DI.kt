package plugins

import domain.repositories.*
import io.ktor.server.application.*
import io.ktor.server.config.*
import io.ktor.server.plugins.di.*
import service.auth.AuthService
import service.cashpool.CashpoolService
import service.cashpool_member.CashpoolMemberService
import service.cashpool_settlement.CashpoolSettlementService
import service.cashpool_settlement.CashpoolSuggestedSettlementCalculationService
import service.cashpool_transaction.CashpoolTransactionService
import service.user.UserService

fun Application.configureDependencyInjection() {
    val env = environment
    dependencies {
        provide<ApplicationConfig> { env.config }

        // Repositories
        provide<UserRepository> { UserRepositoryImpl() }
        provide<CashpoolRepository> { CashpoolRepositoryImpl() }
        provide<CashpoolMemberRepository> { CashpoolMemberRepositoryImpl() }
        provide<CashpoolTransactionRepository> { CashpoolTransactionRepositoryImpl() }
        provide<CashpoolSettlementRepository> { CashpoolSettlementRepositoryImpl() }

        // Services
        provide<UserService> { UserService(resolve()) }
        provide<CashpoolService> { CashpoolService(resolve(), resolve()) }
        provide<CashpoolMemberService> { CashpoolMemberService(resolve(), resolve(), resolve()) }
        provide<CashpoolTransactionService> { CashpoolTransactionService(resolve(), resolve(), resolve()) }
        provide<CashpoolSuggestedSettlementCalculationService> { CashpoolSuggestedSettlementCalculationService(resolve(), resolve(), resolve(), resolve(), resolve()) }
        provide<CashpoolSettlementService> { CashpoolSettlementService(resolve(), resolve()) }
        provide<AuthService> { AuthService(resolve(), resolve()) }
    }
}
