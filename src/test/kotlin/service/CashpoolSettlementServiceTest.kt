package service

import core.exceptions.CashpoolNotFound
import core.exceptions.NotaCashpoolMember
import domain.commands.CreateCashpoolCommand
import domain.commands.CreateCashpoolMemberCommand
import domain.commands.CreateCashpoolSettlementCommand
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
        val cpId = cashpoolService.create(CreateCashpoolCommand("Title", "Desc", ownerId)).id
        cashpoolMemberRepo.create(CreateCashpoolMemberCommand(ownerId, cpId))
        return cpId
    }

    @Test
    fun `create settlement - success`() {
        runBlocking {
            val fromId = userService.create(Commands.User.create()).id
            val toId = userService.create(Commands.User.create()).id
            val cpId = createTestCashpool(fromId)

            cashpoolMemberRepo.create(CreateCashpoolMemberCommand(toId, cpId))

            val cmd = CreateCashpoolSettlementCommand(fromId, toId, cpId, "Purpose", 10_00)
            val settlement = settlementService.create(cmd)

            assertNotNull(settlement)
            assertEquals("Purpose", settlement.purpose)
            assertEquals(10_00, settlement.amountCents)
        }
    }

    @Test
    fun `create settlement - not a member - fails`() {
        runBlocking {
            val ownerId = userService.create(Commands.User.create(email = "owner@ex.com")).id
            val fromId = userService.create(Commands.User.create()).id
            val toId = userService.create(Commands.User.create()).id
            val cpId = createTestCashpool(ownerId)

            val cmd = CreateCashpoolSettlementCommand(fromId, toId, cpId, "Purpose", 10_00)
            assertFailsWith<NotaCashpoolMember> {
                settlementService.create(cmd)
            }
        }
    }

    @Test
    fun `create settlement - cashpool not found - fails`() {
        runBlocking {
            val fromId = userService.create(Commands.User.create()).id
            val toId = userService.create(Commands.User.create()).id
            val cmd = CreateCashpoolSettlementCommand(fromId, toId, -1, "Purpose", 10_00)
            assertFailsWith<CashpoolNotFound> {
                settlementService.create(cmd)
            }
        }
    }

    @Test
    fun `findByCashpoolId - returns settlements`() {
        runBlocking {
            val fromId = userService.create(Commands.User.create()).id
            val toId = userService.create(Commands.User.create()).id
            val ownerId = userService.create(Commands.User.create()).id
            val cpId = createTestCashpool(ownerId)

            cashpoolMemberRepo.create(CreateCashpoolMemberCommand(fromId, cpId))
            cashpoolMemberRepo.create(CreateCashpoolMemberCommand(toId, cpId))
            settlementService.create(CreateCashpoolSettlementCommand(fromId, toId, cpId, "T1", 1000))
            settlementService.create(CreateCashpoolSettlementCommand(fromId, toId, cpId, "T2", 2000))

            val settlements = settlementService.findByCashpoolId(cpId, ownerId)
            assertEquals(2, settlements.size)
        }
    }

    // @Test
    // fun `findByCashpoolId - user not found - fails`() {
    //     runBlocking {
    //         val fromId = userService.create(Commands.User.create()).id
    //         val toId = userService.create(Commands.User.create()).id
    //         val cpId = createTestCashpool(1000)
    //         settlementService.create(CreateCashpoolSettlementCommand(fromId, toId, cpId, "T1", 1000))
    //         settlementService.create(CreateCashpoolSettlementCommand(fromId, toId, cpId, "T2", 2000))
//
    //         assertFailsWith<UserNotFound> {
    //             settlementService.findByCashpoolId(cpId, 1000)
    //         }
    //     }
    // }

    @Test
    fun `findByCashpoolId - not a member - fails`() {
        runBlocking {
            val ownerId = userService.create(Commands.User.create(email = "owner@ex.com")).id
            val otherId = userService.create(Commands.User.create(email = "other@ex.com")).id
            val cpId = createTestCashpool(ownerId)

            assertFailsWith<NotaCashpoolMember> {
                settlementService.findByCashpoolId(cpId, otherId)
            }
        }
    }
}
