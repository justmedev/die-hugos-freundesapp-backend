package service

import kotlinx.coroutines.runBlocking
import kotlin.time.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.junit.Test
import service.cashpool.CashpoolService
import service.cashpool.CreateCashpoolCommand
import service.cashpool_member.CashpoolMemberService
import service.cashpool_member.CreateCashpoolMemberCommand
import service.cashpool_transaction.CashpoolTransactionService
import service.cashpool_transaction.CreateCashpoolTransactionCommand
import service.cashpool_transaction.UpdateCashpoolTransactionCommand
import service.user.CreateUserCommand
import service.user.UserService
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class CashpoolTransactionServiceTest : BaseServiceTest() {
    private val userService = UserService()
    private val cashpoolService = CashpoolService(userService)
    private val cashpoolMemberService = CashpoolMemberService(userService, cashpoolService)
    private val transactionService = CashpoolTransactionService(userService, cashpoolService, cashpoolMemberService)

    private suspend fun createTestUser(email: String = "test@example.com"): Int {
        val now = Clock.System.now().toLocalDateTime(TimeZone.UTC)
        return userService.create(CreateUserCommand(email, "F", "L", "h", now, false)).id
    }

    private suspend fun createTestCashpool(ownerId: Int): Int {
        val cpId = cashpoolService.create(CreateCashpoolCommand("Title", "Desc", ownerId)).id
        cashpoolMemberService.create(CreateCashpoolMemberCommand(ownerId, cpId))
        return cpId
    }

    @Test
    fun `create transaction - success`() {
        runBlocking {
            val userId = createTestUser()
            val cpId = createTestCashpool(userId)
            val cmd = CreateCashpoolTransactionCommand(userId, cpId, "Label", 1000)

            val tx = transactionService.create(cmd)

            assertNotNull(tx)
            assertEquals("Label", tx.label)
            assertEquals(1000, tx.amountCents)
        }
    }

    @Test
    fun `update transaction - success`() {
        runBlocking {
            val userId = createTestUser()
            val cpId = createTestCashpool(userId)
            val tx = transactionService.create(CreateCashpoolTransactionCommand(userId, cpId, "Old", 1000))

            val updateCmd = UpdateCashpoolTransactionCommand(userId, cpId, tx.id, "New", 2000)
            val updated = transactionService.update(updateCmd)

            assertEquals("New", updated.label)
            assertEquals(2000, updated.amountCents)
        }
    }

    @Test
    fun `findByCashpoolId - returns transactions`() {
        runBlocking {
            val userId = createTestUser()
            val cpId = createTestCashpool(userId)
            transactionService.create(CreateCashpoolTransactionCommand(userId, cpId, "T1", 1000))
            transactionService.create(CreateCashpoolTransactionCommand(userId, cpId, "T2", 2000))

            val txs = transactionService.findByCashpoolId(cpId)
            assertEquals(2, txs.size)
        }
    }
}
