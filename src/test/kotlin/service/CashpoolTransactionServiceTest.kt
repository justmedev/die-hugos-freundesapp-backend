package service

import core.exceptions.CashpoolNotFound
import core.exceptions.NotaCashpoolMember
import core.exceptions.TransactionNotFound
import core.exceptions.Unauthorized
import core.utils.UpdateProperty
import domain.commands.*
import domain.contexts.ServiceContext
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
import testutils.Contexts
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
        val cpId = context(Contexts.of(ownerId)) { cashpoolService.create(CreateCashpoolCommand("Title", "Desc", ownerId)).id }
        cashpoolMemberRepo.create(CreateCashpoolMemberCommand(ownerId, cpId))
        return cpId
    }

    @Test
    fun `create transaction - success`() {
        runBlocking {
            val user = context(Contexts.internal) { userService.create(Commands.User.create()) }
            val cpId = createTestCashpool(user.id)
            val cmd = CreateCashpoolTransactionCommand(user.id, cpId, "Label", 1000, listOf(1))

            val tx = context(Contexts.of(user)) { transactionService.create(cmd) }

            assertNotNull(tx)
            assertEquals("Label", tx.label)
            assertEquals(1000, tx.amountCents)
            assertEquals(1, tx.excludedUsers.size)
        }
    }

    @Test
    fun `create transaction - not a member - fails`() {
        runBlocking {
            val owner = context(Contexts.internal) { userService.create(Commands.User.create(email = "owner@ex.com")) }
            val other = context(Contexts.internal) { userService.create(Commands.User.create(email = "other@ex.com")) }
            val cpId = createTestCashpool(owner.id)

            val cmd = CreateCashpoolTransactionCommand(other.id, cpId, "Label", 1000, emptyList())
            assertFailsWith<NotaCashpoolMember> {
                context(Contexts.of(other)) { transactionService.create(cmd) }
            }
        }
    }

    @Test
    fun `update transaction - success`() {
        runBlocking {
            val user = context(Contexts.internal) { userService.create(Commands.User.create()) }
            val cpId = createTestCashpool(user.id)
            val tx = context(Contexts.of(user)) {
                transactionService.create(CreateCashpoolTransactionCommand(user.id, cpId, "Old", 1000, listOf(1, 2)))
            }

            val updateCmd = UpdateCashpoolTransactionCommand(
                user.id,
                cpId,
                tx.id,
                UpdateProperty("New"),
                UpdateProperty(2000L),
                UpdateProperty(null),
                UpdateProperty(listOf(1))
            )
            val updated = context(Contexts.of(user)) { transactionService.update(updateCmd) }

            assertEquals("New", updated.label)
            assertEquals(2000, updated.amountCents)
            assertEquals(1, updated.excludedUsers.size)
        }
    }

    @Test
    fun `update transaction - partial update - success`() {
        runBlocking {
            val user = context(Contexts.internal) { userService.create(Commands.User.create()) }
            val cpId = createTestCashpool(user.id)
            val tx = context(Contexts.of(user)) {
                transactionService.create(CreateCashpoolTransactionCommand(user.id, cpId, "Old", 1000, emptyList()))
            }

            val updateCmd = UpdateCashpoolTransactionCommand(user.id, cpId, tx.id, label = UpdateProperty("New Only"))
            val updated = context(Contexts.of(user)) { transactionService.update(updateCmd) }

            assertEquals("New Only", updated.label)
            assertEquals(1000, updated.amountCents)
        }
    }

    @Test
    fun `update transaction - not found - fails`() {
        runBlocking {
            val user = context(Contexts.internal) { userService.create(Commands.User.create()) }
            val cpId = createTestCashpool(user.id)

            val updateCmd = UpdateCashpoolTransactionCommand(user.id, cpId, 999, UpdateProperty("New"), UpdateProperty(2000L))
            assertFailsWith<TransactionNotFound> {
                context(Contexts.of(user)) { transactionService.update(updateCmd) }
            }
        }
    }

    @Test
    fun `findByCashpoolId - returns transactions`() {
        runBlocking {
            val user = context(Contexts.internal) { userService.create(Commands.User.create()) }
            val cpId = createTestCashpool(user.id)
            context(Contexts.of(user)) {
                transactionService.create(CreateCashpoolTransactionCommand(user.id, cpId, "T1", 1000, emptyList()))
                transactionService.create(CreateCashpoolTransactionCommand(user.id, cpId, "T2", 2000, emptyList()))

                val txs = transactionService.findByCashpoolId(cpId)
                assertEquals(2, txs.size)
            }
        }
    }

    @Test
    fun `findByCashpoolId - not a member - fails`() {
        runBlocking {
            val owner = context(Contexts.internal) { userService.create(Commands.User.create(email = "owner@ex.com")) }
            val other = context(Contexts.internal) { userService.create(Commands.User.create(email = "other@ex.com")) }
            val cpId = createTestCashpool(owner.id)

            assertFailsWith<NotaCashpoolMember> {
                context(Contexts.of(other)) { transactionService.findByCashpoolId(cpId) }
            }
        }
    }

    @Test
    fun `create transaction - cashpool not found - fails`() {
        runBlocking {
            val user = context(Contexts.internal) { userService.create(Commands.User.create()) }
            val cmd = CreateCashpoolTransactionCommand(user.id, 999, "Label", 1000, emptyList())
            assertFailsWith<CashpoolNotFound> {
                context(Contexts.of(user)) { transactionService.create(cmd) }
            }
        }
    }

    @Test
    fun `deleteById - success`() {
        runBlocking {
            val user = context(Contexts.internal) { userService.create(Commands.User.create()) }
            val cpId = createTestCashpool(user.id)
            val tx = context(Contexts.of(user)) {
                transactionService.create(CreateCashpoolTransactionCommand(user.id, cpId, "T1", 1000, emptyList()))
            }

            context(Contexts.of(user)) {
                transactionService.deleteById(cpId, tx.id)
                val txs = transactionService.findByCashpoolId(cpId)
                assertEquals(0, txs.size)
            }
        }
    }

    @Test
    fun `deleteById - not the owner - fails`() {
        runBlocking {
            val owner = context(Contexts.internal) { userService.create(Commands.User.create(email = "owner@ex.com")) }
            val other = context(Contexts.internal) { userService.create(Commands.User.create(email = "other@ex.com")) }
            val cpId = createTestCashpool(owner.id)
            cashpoolMemberRepo.create(CreateCashpoolMemberCommand(other.id, cpId))

            val tx = context(Contexts.of(owner)) {
                transactionService.create(CreateCashpoolTransactionCommand(owner.id, cpId, "T1", 1000, emptyList()))
            }

            assertFailsWith<Unauthorized> {
                context(Contexts.of(other)) { transactionService.deleteById(cpId, tx.id) }
            }
        }
    }

    @Test
    fun `update transaction - not the owner - fails`() {
        runBlocking {
            val owner = context(Contexts.internal) { userService.create(Commands.User.create(email = "owner@ex.com")) }
            val other = context(Contexts.internal) { userService.create(Commands.User.create(email = "other@ex.com")) }
            val cpId = createTestCashpool(owner.id)
            cashpoolMemberRepo.create(CreateCashpoolMemberCommand(other.id, cpId))

            val tx = context(Contexts.of(owner)) {
                transactionService.create(CreateCashpoolTransactionCommand(owner.id, cpId, "Old", 1000, emptyList()))
            }

            val updateCmd = UpdateCashpoolTransactionCommand(other.id, cpId, tx.id, UpdateProperty("New"), UpdateProperty(2000L))
            assertFailsWith<Unauthorized> {
                context(Contexts.of(other)) { transactionService.update(updateCmd) }
            }
        }
    }

    @Test
    fun `create transaction - emits created event`() {
        runBlocking {
            val user = context(Contexts.internal) { userService.create(Commands.User.create()) }
            val cpId = createTestCashpool(user.id)

            val emittedEvents = mutableListOf<CashpoolTransactionEvent>()
            val job = launch {
                transactionService.events.collect { emittedEvents.add(it) }
            }

            yield()

            val cmd = CreateCashpoolTransactionCommand(user.id, cpId, "Label", 1000, emptyList())
            val tx = context(Contexts.of(user)) { transactionService.create(cmd) }

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
            val user = context(Contexts.internal) { userService.create(Commands.User.create()) }
            val cpId = createTestCashpool(user.id)
            val tx = context(Contexts.of(user)) {
                transactionService.create(CreateCashpoolTransactionCommand(user.id, cpId, "Old", 1000, emptyList()))
            }

            val emittedEvents = mutableListOf<CashpoolTransactionEvent>()
            val job = launch {
                transactionService.events.collect { emittedEvents.add(it) }
            }
            yield()

            val updateCmd = UpdateCashpoolTransactionCommand(user.id, cpId, tx.id, UpdateProperty("New"), UpdateProperty(2000L))
            val updated = context(Contexts.of(user)) { transactionService.update(updateCmd) }

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
            val user = context(Contexts.internal) { userService.create(Commands.User.create()) }
            val cpId = createTestCashpool(user.id)
            val tx = context(Contexts.of(user)) {
                transactionService.create(CreateCashpoolTransactionCommand(user.id, cpId, "T1", 1000, emptyList()))
            }

            val emittedEvents = mutableListOf<CashpoolTransactionEvent>()
            val job = launch {
                transactionService.events.collect { emittedEvents.add(it) }
            }
            yield()

            context(Contexts.of(user)) { transactionService.deleteById(cpId, tx.id) }

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
            val user = context(Contexts.internal) { userService.create(Commands.User.create()) }
            val cpId = createTestCashpool(user.id)
            val tx = context(Contexts.of(user)) {
                transactionService.create(CreateCashpoolTransactionCommand(user.id, cpId, "Label", 1000, emptyList()))
            }

            val imageContent = "test-image-content".toByteArray()
            val provider = ByteReadChannel(imageContent)
            val cmd = AttachImageCashpoolTransactionCommand(cpId, tx.id, provider)

            val updated = context(Contexts.of(user)) { transactionService.attachImage(cmd) }

            assertNotNull(updated.attachedImageUUID)
            val imageFile = File("uploads/${updated.attachedImageUUID}")
            kotlin.test.assertTrue(imageFile.exists())

            imageFile.delete()
        }
    }

    @Test
    fun `attachImage - emits updated event`() {
        runBlocking {
            val user = context(Contexts.internal) { userService.create(Commands.User.create()) }
            val cpId = createTestCashpool(user.id)
            val tx = context(Contexts.of(user)) {
                transactionService.create(CreateCashpoolTransactionCommand(user.id, cpId, "Label", 1000, emptyList()))
            }

            val emittedEvents = mutableListOf<CashpoolTransactionEvent>()
            val job = launch {
                transactionService.events.collect { emittedEvents.add(it) }
            }
            yield()

            val provider = ByteReadChannel("image-bytes".toByteArray())
            val cmd = AttachImageCashpoolTransactionCommand(cpId, tx.id, provider)
            val updated = context(Contexts.of(user)) { transactionService.attachImage(cmd) }

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
            val owner = context(Contexts.internal) { userService.create(Commands.User.create(email = "owner@ex.com")) }
            val other = context(Contexts.internal) { userService.create(Commands.User.create(email = "other@ex.com")) }
            val cpId = createTestCashpool(owner.id)
            val tx = context(Contexts.of(owner)) {
                transactionService.create(CreateCashpoolTransactionCommand(owner.id, cpId, "Label", 1000, emptyList()))
            }

            val provider = ByteReadChannel("image-bytes".toByteArray())
            val cmd = AttachImageCashpoolTransactionCommand(cpId, tx.id, provider)
            assertFailsWith<NotaCashpoolMember> {
                context(Contexts.of(other)) { transactionService.attachImage(cmd) }
            }
        }
    }

    @Test
    fun `attachImage - transaction not found - fails`() {
        runBlocking {
            val user = context(Contexts.internal) { userService.create(Commands.User.create()) }
            val cpId = createTestCashpool(user.id)

            val provider = ByteReadChannel("image-bytes".toByteArray())
            val cmd = AttachImageCashpoolTransactionCommand(cpId, 999, provider)
            assertFailsWith<TransactionNotFound> {
                context(Contexts.of(user)) { transactionService.attachImage(cmd) }
            }
        }
    }

    @Test
    fun `attachImage - not the owner - fails`() {
        runBlocking {
            val owner = context(Contexts.internal) { userService.create(Commands.User.create(email = "owner@ex.com")) }
            val other = context(Contexts.internal) { userService.create(Commands.User.create(email = "other@ex.com")) }
            val cpId = createTestCashpool(owner.id)
            cashpoolMemberRepo.create(CreateCashpoolMemberCommand(other.id, cpId))

            val tx = context(Contexts.of(owner)) {
                transactionService.create(CreateCashpoolTransactionCommand(owner.id, cpId, "Label", 1000, emptyList()))
            }

            val provider = ByteReadChannel("image-bytes".toByteArray())
            val cmd = AttachImageCashpoolTransactionCommand(cpId, tx.id, provider)
            assertFailsWith<Unauthorized> {
                context(Contexts.of(other)) { transactionService.attachImage(cmd) }
            }
        }
    }

    @Test
    fun `deleteById - deletes attached image file if present`() {
        runBlocking {
            val user = context(Contexts.internal) { userService.create(Commands.User.create()) }
            val cpId = createTestCashpool(user.id)
            val tx = context(Contexts.of(user)) {
                transactionService.create(CreateCashpoolTransactionCommand(user.id, cpId, "T1", 1000, emptyList()))
            }

            val provider = ByteReadChannel("image-bytes".toByteArray())
            val updated = context(Contexts.of(user)) {
                transactionService.attachImage(AttachImageCashpoolTransactionCommand(cpId, tx.id, provider))
            }
            val imageFile = File("uploads/${updated.attachedImageUUID}")
            kotlin.test.assertTrue(imageFile.exists())

            context(Contexts.of(user)) { transactionService.deleteById(cpId, tx.id) }

            kotlin.test.assertFalse(imageFile.exists())
        }
    }
}