package service

import kotlinx.coroutines.runBlocking
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.junit.Test
import service.cashpool.CashpoolService
import service.cashpool_member.CashpoolMemberService
import service.cashpool_settlement.CashpoolSettlementService
import service.cashpool_transaction.CashpoolTransactionService
import service.user.UserService
import kotlin.test.assertEquals
import kotlin.time.Clock

class CashpoolSettlementServiceTest : BaseServiceTest() {
    private val userService = UserService()
    private val cashpoolService = CashpoolService(userService)
    private val cashpoolMemberService = CashpoolMemberService(userService, cashpoolService)
    private val transactionService = CashpoolTransactionService(userService, cashpoolService, cashpoolMemberService)
    private val settlementService =
        CashpoolSettlementService(cashpoolService, transactionService, cashpoolMemberService)

    @Test
    fun `calculateSettlements - empty result (not fully implemented)`() {
        runBlocking {
            val now = Clock.System.now().toLocalDateTime(TimeZone.UTC)
            val userId = userService.create(service.user.CreateUserCommand("t@e.com", "F", "L", "h", now, false)).id
            val cpId = cashpoolService.create(service.cashpool.CreateCashpoolCommand("T", "D", userId)).id

            val result = settlementService.calculateSettlements(cpId)
            assertEquals(0, result.size)
        }
    }
}
