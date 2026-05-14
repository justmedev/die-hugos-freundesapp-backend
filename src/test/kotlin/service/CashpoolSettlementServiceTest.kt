package service

import kotlinx.coroutines.runBlocking
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.junit.Test
import service.cashpool.CashpoolService
import domain.commands.CreateCashpoolCommand
import service.cashpool_member.CashpoolMemberService
import domain.commands.CreateCashpoolMemberCommand
import service.cashpool_settlement.CashpoolSettlementService
import service.cashpool_transaction.CashpoolTransactionService
import domain.commands.CreateCashpoolTransactionCommand
import domain.commands.CreateUserCommand
import service.user.UserService
import kotlin.test.assertEquals
import kotlin.time.Clock

import core.exceptions.CashpoolNotFound
import domain.repositories.CashpoolMemberRepositoryImpl
import domain.repositories.CashpoolRepositoryImpl
import domain.repositories.CashpoolTransactionRepositoryImpl
import domain.repositories.UserRepositoryImpl
import testutils.Commands
import kotlin.test.assertFailsWith

class CashpoolSettlementServiceTest : BaseServiceTest() {
    private val userRepo = UserRepositoryImpl()
    private val userService = UserService(userRepo)
    private val cashpoolRepo = CashpoolRepositoryImpl()
    private val cashpoolService = CashpoolService(userService, cashpoolRepo)
    private val cashpoolMemberRepo = CashpoolMemberRepositoryImpl()
    private val cashpoolMemberService = CashpoolMemberService(cashpoolMemberRepo, userRepo, cashpoolRepo)
    private val transactionRepo = CashpoolTransactionRepositoryImpl()
    private val transactionService = CashpoolTransactionService(transactionRepo, cashpoolRepo)
    private val settlementService =
        CashpoolSettlementService(cashpoolService, transactionService, cashpoolMemberService)

    @Test
    fun `calculateSettlements - success`() {
        runBlocking {
            val now = Clock.System.now().toLocalDateTime(TimeZone.UTC)

            val userAId = userService.create(Commands.User.create(firstName = "Sarah")).id
            val userBId = userService.create(Commands.User.create(firstName = "Elias")).id
            val userCId = userService.create(Commands.User.create(firstName = "Leon")).id
            val userDId = userService.create(Commands.User.create(firstName = "Donald")).id
            val userEId = userService.create(Commands.User.create(firstName = "Mina")).id
            val users = listOf(userAId, userBId, userCId, userDId, userEId)
            val cpId = cashpoolService.create(CreateCashpoolCommand("T", "D", userAId)).id

            users.forEach { cashpoolMemberRepo.create(CreateCashpoolMemberCommand(it, cpId)) }

            createTransaction(cpId, userAId, 50_00)
            createTransaction(cpId, userBId, 80_41)
            createTransaction(cpId, userCId, 93_65)
            createTransaction(cpId, userDId, -20_00)
            createTransaction(cpId, userEId, 0)

            val result = settlementService.calculateSettlements(cpId)

            assertEquals(4, result.size)

            // donald sends leon 52.84€
            assertEquals(userDId, result[0].from.id)
            assertEquals(userCId, result[0].to.id)
            assertEquals(52_84, result[0].amountCents)
        }
    }

    @Test
    fun `calculateSettlements - not found - fails`() {
        runBlocking {
            assertFailsWith<CashpoolNotFound> {
                settlementService.calculateSettlements(999)
            }
        }
    }

    suspend fun createTransaction(cashpoolId: Int, userId: Int, amountCents: Long) {
        transactionService.create(CreateCashpoolTransactionCommand(userId, cashpoolId, "T1", amountCents))
    }
}
