package service

import core.exceptions.CashpoolNotFound
import core.exceptions.NotaCashpoolMember
import domain.commands.CreateCashpoolCommand
import domain.commands.CreateCashpoolMemberCommand
import domain.commands.CreateCashpoolSettlementCommand
import domain.contexts.ServiceContext
import domain.repositories.CashpoolMemberRepositoryImpl
import domain.repositories.CashpoolRepositoryImpl
import domain.repositories.CashpoolSettlementRepositoryImpl
import domain.repositories.UserRepositoryImpl
import kotlinx.coroutines.runBlocking
import org.junit.Test
import service.cashpool.CashpoolService
import service.cashpool_settlement.CashpoolSettlementService
import service.user.UserService
import testutils.Commands
import testutils.Contexts
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull

class CashpoolSettlementServiceTest : BaseServiceTest() {
    private val userRepo = UserRepositoryImpl()
    private val userService = UserService(userRepo)
    private val cashpoolRepo = CashpoolRepositoryImpl()
    private val settlementRepo = CashpoolSettlementRepositoryImpl()
    private val cashpoolService = CashpoolService(userService, cashpoolRepo)
    private val cashpoolMemberRepo = CashpoolMemberRepositoryImpl()
    private val settlementService = CashpoolSettlementService(settlementRepo, cashpoolService)

    private suspend fun createTestCashpool(ownerId: Int): Int {
        val cpId = context(Contexts.of(ownerId)) { cashpoolService.create(CreateCashpoolCommand("Title", "Desc", ownerId)).id }
        cashpoolMemberRepo.create(CreateCashpoolMemberCommand(ownerId, cpId))
        return cpId
    }

    @Test
    fun `create settlement - success`() {
        runBlocking {
            val fromUser = context(Contexts.internal) { userService.create(Commands.User.create()) }
            val toUser = context(Contexts.internal) { userService.create(Commands.User.create()) }
            val cpId = createTestCashpool(fromUser.id)

            cashpoolMemberRepo.create(CreateCashpoolMemberCommand(toUser.id, cpId))

            val cmd = CreateCashpoolSettlementCommand(fromUser.id, toUser.id, cpId, "Purpose", 10_00)
            val settlement = context(Contexts.of(fromUser)) { settlementService.create(cmd) }

            assertNotNull(settlement)
            assertEquals("Purpose", settlement.purpose)
            assertEquals(10_00, settlement.amountCents)
        }
    }

    @Test
    fun `create settlement - not a member - fails`() {
        runBlocking {
            val owner = context(Contexts.internal) { userService.create(Commands.User.create(email = "owner@ex.com")) }
            val fromUser = context(Contexts.internal) { userService.create(Commands.User.create()) }
            val toUser = context(Contexts.internal) { userService.create(Commands.User.create()) }
            val cpId = createTestCashpool(owner.id)

            val cmd = CreateCashpoolSettlementCommand(fromUser.id, toUser.id, cpId, "Purpose", 10_00)
            assertFailsWith<NotaCashpoolMember> {
                context(Contexts.of(fromUser)) { settlementService.create(cmd) }
            }
        }
    }

    @Test
    fun `create settlement - cashpool not found - fails`() {
        runBlocking {
            val fromUser = context(Contexts.internal) { userService.create(Commands.User.create()) }
            val toUser = context(Contexts.internal) { userService.create(Commands.User.create()) }
            val cmd = CreateCashpoolSettlementCommand(fromUser.id, toUser.id, -1, "Purpose", 10_00)
            assertFailsWith<CashpoolNotFound> {
                context(Contexts.internal) { settlementService.create(cmd) }
            }
        }
    }

    @Test
    fun `findByCashpoolId - returns settlements`() {
        runBlocking {
            val fromUser = context(Contexts.internal) { userService.create(Commands.User.create()) }
            val toUser = context(Contexts.internal) { userService.create(Commands.User.create()) }
            val owner = context(Contexts.internal) { userService.create(Commands.User.create()) }
            val cpId = createTestCashpool(owner.id)

            cashpoolMemberRepo.create(CreateCashpoolMemberCommand(fromUser.id, cpId))
            cashpoolMemberRepo.create(CreateCashpoolMemberCommand(toUser.id, cpId))
            context(Contexts.of(fromUser)) {
                settlementService.create(CreateCashpoolSettlementCommand(fromUser.id, toUser.id, cpId, "T1", 1000))
                settlementService.create(CreateCashpoolSettlementCommand(fromUser.id, toUser.id, cpId, "T2", 2000))
            }

            val settlements = context(Contexts.of(owner)) { settlementService.findByCashpoolId(cpId) }
            assertEquals(2, settlements.size)
        }
    }

    @Test
    fun `findByCashpoolId - not a member - fails`() {
        runBlocking {
            val owner = context(Contexts.internal) { userService.create(Commands.User.create(email = "owner@ex.com")) }
            val other = context(Contexts.internal) { userService.create(Commands.User.create(email = "other@ex.com")) }
            val cpId = createTestCashpool(owner.id)

            assertFailsWith<NotaCashpoolMember> {
                context(Contexts.of(other)) { settlementService.findByCashpoolId(cpId) }
            }
        }
    }
}
