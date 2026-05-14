package service

import core.exceptions.CashpoolNotFound
import core.exceptions.NotaCashpoolMember
import core.exceptions.TransactionNotFound
import core.exceptions.Unauthorized
import domain.commands.CreateCashpoolCommand
import domain.commands.CreateCashpoolMemberCommand
import domain.commands.CreateCashpoolTransactionCommand
import domain.commands.UpdateCashpoolTransactionCommand
import domain.repositories.CashpoolMemberRepositoryImpl
import domain.repositories.CashpoolRepositoryImpl
import domain.repositories.CashpoolTransactionRepositoryImpl
import domain.repositories.UserRepositoryImpl
import kotlinx.coroutines.runBlocking
import org.junit.Test
import service.cashpool.CashpoolService
import service.cashpool_transaction.CashpoolTransactionService
import service.user.UserService
import testutils.Commands
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull

class CashpoolTransactionServiceTest : BaseServiceTest() {
    private val userRepo = UserRepositoryImpl()
    private val userService = UserService(userRepo)
    private val cashpoolRepo = CashpoolRepositoryImpl()
    private val cashpoolService = CashpoolService(userService, cashpoolRepo)
    private val cashpoolMemberRepo = CashpoolMemberRepositoryImpl()
    private val transactionRepo = CashpoolTransactionRepositoryImpl()
    private val transactionService = CashpoolTransactionService(transactionRepo, cashpoolRepo, userService)

    private suspend fun createTestCashpool(ownerId: Int): Int {
        val cpId = cashpoolService.create(CreateCashpoolCommand("Title", "Desc", ownerId)).id
        cashpoolMemberRepo.create(CreateCashpoolMemberCommand(ownerId, cpId))
        return cpId
    }

    @Test
    fun `create transaction - success`() {
        runBlocking {
            val userId = userService.create(Commands.User.create()).id
            val cpId = createTestCashpool(userId)
            val cmd = CreateCashpoolTransactionCommand(userId, cpId, "Label", 1000)

            val tx = transactionService.create(cmd)

            assertNotNull(tx)
            assertEquals("Label", tx.label)
            assertEquals(1000, tx.amountCents)
        }
    }

    @Test
    fun `create transaction - not a member - fails`() {
        runBlocking {
            val ownerId = userService.create(Commands.User.create(email = "owner@ex.com")).id
            val otherId = userService.create(Commands.User.create(email = "other@ex.com")).id
            val cpId = createTestCashpool(ownerId)

            val cmd = CreateCashpoolTransactionCommand(otherId, cpId, "Label", 1000)
            assertFailsWith<NotaCashpoolMember> {
                transactionService.create(cmd)
            }
        }
    }

    @Test
    fun `update transaction - success`() {
        runBlocking {
            val userId = userService.create(Commands.User.create()).id
            val cpId = createTestCashpool(userId)
            val tx = transactionService.create(CreateCashpoolTransactionCommand(userId, cpId, "Old", 1000))

            val updateCmd = UpdateCashpoolTransactionCommand(userId, cpId, tx.id, "New", 2000)
            val updated = transactionService.update(updateCmd)

            assertEquals("New", updated.label)
            assertEquals(2000, updated.amountCents)
        }
    }

    @Test
    fun `update transaction - not found - fails`() {
        runBlocking {
            val userId = userService.create(Commands.User.create()).id
            val cpId = createTestCashpool(userId)

            val updateCmd = UpdateCashpoolTransactionCommand(userId, cpId, 999, "New", 2000)
            assertFailsWith<TransactionNotFound> {
                transactionService.update(updateCmd)
            }
        }
    }

    @Test
    fun `findByCashpoolId - returns transactions`() {
        runBlocking {
            val userId = userService.create(Commands.User.create()).id
            val cpId = createTestCashpool(userId)
            transactionService.create(CreateCashpoolTransactionCommand(userId, cpId, "T1", 1000))
            transactionService.create(CreateCashpoolTransactionCommand(userId, cpId, "T2", 2000))

            val txs = transactionService.findByCashpoolId(cpId, userId)
            assertEquals(2, txs.size)
        }
    }

    @Test
    fun `findByCashpoolId - not a member - fails`() {
        runBlocking {
            val ownerId = userService.create(Commands.User.create(email = "owner@ex.com")).id
            val otherId = userService.create(Commands.User.create(email = "other@ex.com")).id
            val cpId = createTestCashpool(ownerId)

            assertFailsWith<NotaCashpoolMember> {
                transactionService.findByCashpoolId(cpId, otherId)
            }
        }
    }

    @Test
    fun `create transaction - cashpool not found - fails`() {
        runBlocking {
            val userId = userService.create(Commands.User.create()).id
            val cmd = CreateCashpoolTransactionCommand(userId, 999, "Label", 1000)
            assertFailsWith<CashpoolNotFound> {
                transactionService.create(cmd)
            }
        }
    }

    @Test
    fun `deleteById - success`() {
        runBlocking {
            val userId = userService.create(Commands.User.create()).id
            val cpId = createTestCashpool(userId)
            val tx = transactionService.create(CreateCashpoolTransactionCommand(userId, cpId, "T1", 1000))

            transactionService.deleteById(cpId, tx.id, userId)

            val txs = transactionService.findByCashpoolId(cpId, userId)
            assertEquals(0, txs.size)
        }
    }

    @Test
    fun `deleteById - not the owner - fails`() {
        runBlocking {
            val ownerId = userService.create(Commands.User.create(email = "owner@ex.com")).id
            val otherId = userService.create(Commands.User.create(email = "other@ex.com")).id
            val cpId = createTestCashpool(ownerId)
            cashpoolMemberRepo.create(CreateCashpoolMemberCommand(otherId, cpId))

            val tx = transactionService.create(CreateCashpoolTransactionCommand(ownerId, cpId, "T1", 1000))

            // This should fail because otherId is not the owner of tx
            assertFailsWith<Unauthorized> {
                transactionService.deleteById(cpId, tx.id, otherId)
            }
        }
    }

    @Test
    fun `update transaction - not the owner - fails`() {
        runBlocking {
            val ownerId = userService.create(Commands.User.create(email = "owner@ex.com")).id
            val otherId = userService.create(Commands.User.create(email = "other@ex.com")).id
            val cpId = createTestCashpool(ownerId)
            cashpoolMemberRepo.create(CreateCashpoolMemberCommand(otherId, cpId))

            val tx = transactionService.create(CreateCashpoolTransactionCommand(ownerId, cpId, "Old", 1000))

            val updateCmd = UpdateCashpoolTransactionCommand(otherId, cpId, tx.id, "New", 2000)
            // This should fail because otherId is not the owner of tx
            assertFailsWith<Unauthorized> {
                transactionService.update(updateCmd)
            }
        }
    }
}
