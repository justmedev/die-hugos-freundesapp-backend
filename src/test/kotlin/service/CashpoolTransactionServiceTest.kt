package service

import core.exceptions.CashpoolNotFound
import core.exceptions.NotaCashpoolMember
import core.exceptions.TransactionNotFound
import core.exceptions.Unauthorized
import core.utils.UpdateProperty
import domain.commands.*
import domain.models.events.CashpoolTransactionEvent
import domain.repositories.CashpoolMemberRepositoryImpl
import domain.repositories.CashpoolRepositoryImpl
import domain.repositories.CashpoolTransactionRepositoryImpl
import domain.repositories.UserRepositoryImpl
import io.ktor.utils.io.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.yield
import org.junit.Test
import service.cashpool.CashpoolService
import service.cashpool_transaction.CashpoolTransactionService
import service.user.UserService
import testutils.Commands
import java.io.File
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
    private val transactionService = CashpoolTransactionService(transactionRepo, cashpoolService, userService)

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
            val cmd = CreateCashpoolTransactionCommand(userId, cpId, "Label", 1000, listOf(1))

            val tx = transactionService.create(cmd)

            assertNotNull(tx)
            assertEquals("Label", tx.label)
            assertEquals(1000, tx.amountCents)
            assertEquals(1, tx.excludedUsers.size)
        }
    }

    @Test
    fun `create transaction - not a member - fails`() {
        runBlocking {
            val ownerId = userService.create(Commands.User.create(email = "owner@ex.com")).id
            val otherId = userService.create(Commands.User.create(email = "other@ex.com")).id
            val cpId = createTestCashpool(ownerId)

            val cmd = CreateCashpoolTransactionCommand(otherId, cpId, "Label", 1000, emptyList())
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
            val tx =
                transactionService.create(CreateCashpoolTransactionCommand(userId, cpId, "Old", 1000, listOf(1, 2)))

            val updateCmd = UpdateCashpoolTransactionCommand(
                userId,
                cpId,
                tx.id,
                UpdateProperty("New"),
                UpdateProperty(2000L),
                UpdateProperty(null),
                UpdateProperty(listOf(1))
            )
            val updated = transactionService.update(updateCmd)

            assertEquals("New", updated.label)
            assertEquals(2000, updated.amountCents)
            assertEquals(1, updated.excludedUsers.size)
        }
    }

    @Test
    fun `update transaction - partial update - success`() {
        runBlocking {
            val userId = userService.create(Commands.User.create()).id
            val cpId = createTestCashpool(userId)
            val tx = transactionService.create(CreateCashpoolTransactionCommand(userId, cpId, "Old", 1000, emptyList()))

            val updateCmd = UpdateCashpoolTransactionCommand(userId, cpId, tx.id, label = UpdateProperty("New Only"))
            val updated = transactionService.update(updateCmd)

            assertEquals("New Only", updated.label)
            assertEquals(1000, updated.amountCents)
        }
    }

    @Test
    fun `update transaction - not found - fails`() {
        runBlocking {
            val userId = userService.create(Commands.User.create()).id
            val cpId = createTestCashpool(userId)

            val updateCmd = UpdateCashpoolTransactionCommand(userId, cpId, 999, UpdateProperty("New"), UpdateProperty(2000L))
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
            transactionService.create(CreateCashpoolTransactionCommand(userId, cpId, "T1", 1000, emptyList()))
            transactionService.create(CreateCashpoolTransactionCommand(userId, cpId, "T2", 2000, emptyList()))

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
            val cmd = CreateCashpoolTransactionCommand(userId, 999, "Label", 1000, emptyList())
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
            val tx = transactionService.create(CreateCashpoolTransactionCommand(userId, cpId, "T1", 1000, emptyList()))

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

            val tx = transactionService.create(CreateCashpoolTransactionCommand(ownerId, cpId, "T1", 1000, emptyList()))

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

            val tx =
                transactionService.create(CreateCashpoolTransactionCommand(ownerId, cpId, "Old", 1000, emptyList()))

            val updateCmd = UpdateCashpoolTransactionCommand(otherId, cpId, tx.id, UpdateProperty("New"), UpdateProperty(2000L))
            // This should fail because otherId is not the owner of tx
            assertFailsWith<Unauthorized> {
                transactionService.update(updateCmd)
            }
        }
    }

    @Test
    fun `create transaction - emits created event`() {
        runBlocking {
            val userId = userService.create(Commands.User.create()).id
            val cpId = createTestCashpool(userId)

            val emittedEvents = mutableListOf<CashpoolTransactionEvent>()
            val job = launch {
                transactionService.events.collect { emittedEvents.add(it) }
            }

            // Allow collector coroutine to start up and reach the collect() point
            yield()

            val cmd = CreateCashpoolTransactionCommand(userId, cpId, "Label", 1000, emptyList())
            val tx = transactionService.create(cmd)

            // Let the collector coroutine process the emitted event
            yield()
            job.cancel()

            assertEquals(1, emittedEvents.size)
            val event = emittedEvents.first() as CashpoolTransactionEvent.Created
            assertEquals(cpId, event.cashpoolId)
            assertEquals(tx.id, event.transaction.id)
            assertEquals("Label", event.transaction.label)
        }
    }

    @Test
    fun `update transaction - emits updated event`() {
        runBlocking {
            val userId = userService.create(Commands.User.create()).id
            val cpId = createTestCashpool(userId)
            val tx = transactionService.create(CreateCashpoolTransactionCommand(userId, cpId, "Old", 1000, emptyList()))

            val emittedEvents = mutableListOf<CashpoolTransactionEvent>()
            val job = launch {
                transactionService.events.collect { emittedEvents.add(it) }
            }
            yield()

            val updateCmd = UpdateCashpoolTransactionCommand(userId, cpId, tx.id, UpdateProperty("New"), UpdateProperty(2000L))
            val updated = transactionService.update(updateCmd)

            yield()
            job.cancel()

            assertEquals(1, emittedEvents.size)
            val event = emittedEvents.first() as CashpoolTransactionEvent.Updated
            assertEquals(cpId, event.cashpoolId)
            assertEquals(updated.id, event.transaction.id)
            assertEquals("New", event.transaction.label)
        }
    }

    @Test
    fun `deleteById - emits deleted event`() {
        runBlocking {
            val userId = userService.create(Commands.User.create()).id
            val cpId = createTestCashpool(userId)
            val tx = transactionService.create(CreateCashpoolTransactionCommand(userId, cpId, "T1", 1000, emptyList()))

            val emittedEvents = mutableListOf<CashpoolTransactionEvent>()
            val job = launch {
                transactionService.events.collect { emittedEvents.add(it) }
            }
            yield()

            transactionService.deleteById(cpId, tx.id, userId)

            yield()
            job.cancel()

            assertEquals(1, emittedEvents.size)
            val event = emittedEvents.first() as CashpoolTransactionEvent.Deleted
            assertEquals(cpId, event.cashpoolId)
            assertEquals(tx.id, event.transactionId)
        }
    }

    @Test
    fun `attachImage - success`() {
        runBlocking {
            val userId = userService.create(Commands.User.create()).id
            val cpId = createTestCashpool(userId)
            val tx =
                transactionService.create(CreateCashpoolTransactionCommand(userId, cpId, "Label", 1000, emptyList()))

            val imageContent = "test-image-content".toByteArray()
            val provider = ByteReadChannel(imageContent)
            val cmd = AttachImageCashpoolTransactionCommand(userId, cpId, tx.id, provider)

            val updated = transactionService.attachImage(cmd)

            assertNotNull(updated.attachedImageUUID)
            val imageFile = File("uploads/${updated.attachedImageUUID}")
            kotlin.test.assertTrue(imageFile.exists())

            imageFile.delete()
        }
    }

    @Test
    fun `attachImage - emits updated event`() {
        runBlocking {
            val userId = userService.create(Commands.User.create()).id
            val cpId = createTestCashpool(userId)
            val tx =
                transactionService.create(CreateCashpoolTransactionCommand(userId, cpId, "Label", 1000, emptyList()))

            val emittedEvents = mutableListOf<CashpoolTransactionEvent>()
            val job = launch {
                transactionService.events.collect { emittedEvents.add(it) }
            }
            yield()

            val provider = ByteReadChannel("image-bytes".toByteArray())
            val cmd = AttachImageCashpoolTransactionCommand(userId, cpId, tx.id, provider)
            val updated = transactionService.attachImage(cmd)

            yield()
            job.cancel()

            File("uploads/${updated.attachedImageUUID}").delete()

            assertEquals(1, emittedEvents.size)
            val event = emittedEvents.first() as CashpoolTransactionEvent.Updated
            assertEquals(cpId, event.cashpoolId)
            assertEquals(updated.id, event.transaction.id)
            assertEquals(updated.attachedImageUUID, event.transaction.attachedImageUUID)
        }
    }

    @Test
    fun `attachImage - not a member - fails`() {
        runBlocking {
            val ownerId = userService.create(Commands.User.create(email = "owner@ex.com")).id
            val otherId = userService.create(Commands.User.create(email = "other@ex.com")).id
            val cpId = createTestCashpool(ownerId)
            val tx =
                transactionService.create(CreateCashpoolTransactionCommand(ownerId, cpId, "Label", 1000, emptyList()))

            val provider = ByteReadChannel("image-bytes".toByteArray())
            val cmd = AttachImageCashpoolTransactionCommand(otherId, cpId, tx.id, provider)
            assertFailsWith<NotaCashpoolMember> {
                transactionService.attachImage(cmd)
            }
        }
    }

    @Test
    fun `attachImage - transaction not found - fails`() {
        runBlocking {
            val userId = userService.create(Commands.User.create()).id
            val cpId = createTestCashpool(userId)

            val provider = ByteReadChannel("image-bytes".toByteArray())
            val cmd = AttachImageCashpoolTransactionCommand(userId, cpId, 999, provider)
            assertFailsWith<TransactionNotFound> {
                transactionService.attachImage(cmd)
            }
        }
    }

    @Test
    fun `attachImage - not the owner - fails`() {
        runBlocking {
            val ownerId = userService.create(Commands.User.create(email = "owner@ex.com")).id
            val otherId = userService.create(Commands.User.create(email = "other@ex.com")).id
            val cpId = createTestCashpool(ownerId)
            cashpoolMemberRepo.create(CreateCashpoolMemberCommand(otherId, cpId))

            val tx =
                transactionService.create(CreateCashpoolTransactionCommand(ownerId, cpId, "Label", 1000, emptyList()))

            val provider = ByteReadChannel("image-bytes".toByteArray())
            val cmd = AttachImageCashpoolTransactionCommand(otherId, cpId, tx.id, provider)
            assertFailsWith<Unauthorized> {
                transactionService.attachImage(cmd)
            }
        }
    }

    @Test
    fun `deleteById - deletes attached image file if present`() {
        runBlocking {
            val userId = userService.create(Commands.User.create()).id
            val cpId = createTestCashpool(userId)
            val tx = transactionService.create(CreateCashpoolTransactionCommand(userId, cpId, "T1", 1000, emptyList()))

            val provider = ByteReadChannel("image-bytes".toByteArray())
            val updated = transactionService.attachImage(AttachImageCashpoolTransactionCommand(userId, cpId, tx.id, provider))
            val imageFile = File("uploads/${updated.attachedImageUUID}")
            kotlin.test.assertTrue(imageFile.exists())

            transactionService.deleteById(cpId, tx.id, userId)

            kotlin.test.assertFalse(imageFile.exists())
        }
    }
}