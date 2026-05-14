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

    @Test
    fun `calculateSettlements - empty members - returns empty list`() {
        runBlocking {
            val ownerId = userService.create(Commands.User.create()).id
            val cpId = cashpoolService.create(CreateCashpoolCommand("T", "D", ownerId)).id
            // No members added (not even owner)

            val result = settlementService.calculateSettlements(cpId)
            assertEquals(0, result.size)
        }
    }

    @Test
    fun `calculateSettlements - more credit than debt scenario`() {
        runBlocking {
            // Setup a scenario where rounding or specific amounts might leave creditors in the while loop
            // In the provided service logic, debtSum and creditSum are checked for tolerance.
            // Let's create a 3 member pool where 2 paid and 1 didn't.
            val u1 = userService.create(Commands.User.create(firstName = "U1", email = "u1@ex.com")).id
            val u2 = userService.create(Commands.User.create(firstName = "U2", email = "u2@ex.com")).id
            val u3 = userService.create(Commands.User.create(firstName = "U3", email = "u3@ex.com")).id
            val cpId = cashpoolService.create(CreateCashpoolCommand("T", "D", u1)).id

            listOf(u1, u2, u3).forEach { cashpoolMemberRepo.create(CreateCashpoolMemberCommand(it, cpId)) }

            // Total 30.00, fair share 10.00
            createTransaction(cpId, u1, 20_00) // Creditor (+10.00)
            createTransaction(cpId, u2, 10_01) // Creditor (+0.01) - This might trigger rounding logic
            createTransaction(cpId, u3, 0)     // Debtor (-10.00 approx)
            
            // Total = 30.01. Fair share = 30.01 / 3 = 10.0033... -> 10.00 (HALF_UP)
            // Debtors: U3 (needs to pay 10.00)
            // Creditors: U1 (paid 20, share 10 -> due 10), U2 (paid 10.01, share 10 -> due 0.01)
            // This tests the loop where one debtor might resolve multiple creditors or vice versa.

            val result = settlementService.calculateSettlements(cpId)
            
            // Should have 1 settlement (U3 -> U1). U2 remains a creditor by 0.01, which is within tolerance.
            assertEquals(1, result.size)
        }
    }

    suspend fun createTransaction(cashpoolId: Int, userId: Int, amountCents: Long) {
        transactionService.create(CreateCashpoolTransactionCommand(userId, cashpoolId, "T1", amountCents))
    }
}
