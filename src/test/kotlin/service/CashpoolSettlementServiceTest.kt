package service

import kotlinx.coroutines.runBlocking
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.junit.Test
import service.cashpool.CashpoolService
import service.cashpool.CreateCashpoolCommand
import service.cashpool_member.CashpoolMemberService
import service.cashpool_member.CreateCashpoolMemberCommand
import service.cashpool_settlement.CashpoolSettlementService
import service.cashpool_transaction.CashpoolTransactionService
import service.cashpool_transaction.CreateCashpoolTransactionCommand
import service.user.CreateUserCommand
import service.user.UserService
import kotlin.test.assertEquals
import kotlin.time.Clock

class CashpoolSettlementServiceTest : BaseServiceTest() {
    private val userService = UserService()
    private val cashpoolService = CashpoolService(userService)
    private val cashpoolMemberService = CashpoolMemberService(userService, cashpoolService)
    private val cashpoolTransactionService =
        CashpoolTransactionService(userService, cashpoolService, cashpoolMemberService)
    private val transactionService = CashpoolTransactionService(userService, cashpoolService, cashpoolMemberService)
    private val settlementService =
        CashpoolSettlementService(cashpoolService, transactionService, cashpoolMemberService)

    @Test
    fun `calculateSettlements - empty result (not fully implemented)`() {
        runBlocking {
            val now = Clock.System.now().toLocalDateTime(TimeZone.UTC)


            val userAId = userService.create(CreateUserCommand("a@a.com", "Sarah", "A", "h", now, false)).id
            val userBId = userService.create(CreateUserCommand("b@b.com", "Elias", "B", "h", now, false)).id
            val userCId = userService.create(CreateUserCommand("c@c.com", "Leon", "C", "h", now, false)).id
            val userDId = userService.create(CreateUserCommand("d@d.com", "Donald", "D", "h", now, false)).id
            val userEId = userService.create(CreateUserCommand("e@e.com", "Mina", "E", "h", now, false)).id
            val users = listOf(userAId, userBId, userCId, userDId, userEId)
            val cpId = cashpoolService.create(CreateCashpoolCommand("T", "D", userAId)).id

            users.forEach { cashpoolMemberService.create(CreateCashpoolMemberCommand(it, cpId)) }

            createTransaction(cpId, userAId, 50_00)
            createTransaction(cpId, userBId, 80_41)
            createTransaction(cpId, userCId, 93_65)
            createTransaction(cpId, userDId, -20_00)
            createTransaction(cpId, userEId, 0)

            val result = settlementService.calculateSettlements(cpId)

            // 350 total -> 350/2 = 175 per Person, so userB should pay 125 to userA
            assertEquals(4, result.size)

            // donald sends leon 52.84€
            assertEquals(userDId, result[0].from.id)
            assertEquals(userCId, result[0].to.id)
            assertEquals(52_84, result[0].amountCents)

            // mina sends elias 39.60€
            assertEquals(userEId, result[1].from.id)
            assertEquals(userBId, result[1].to.id)
            assertEquals(39_60, result[1].amountCents)

            // donald sends sarah 7.97€
            assertEquals(userDId, result[2].from.id)
            assertEquals(userAId, result[2].to.id)
            assertEquals(7_97, result[2].amountCents)

            // mina sends sarah 1.21€
            assertEquals(userEId, result[3].from.id)
            assertEquals(userAId, result[3].to.id)
            assertEquals(1_21, result[3].amountCents)
        }
    }

    suspend fun createTransaction(cashpoolId: Int, userId: Int, amountCents: Long) {
        cashpoolTransactionService.create(CreateCashpoolTransactionCommand(userId, cashpoolId, "T1", amountCents))
    }
}
