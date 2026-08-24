package service

import kotlinx.coroutines.runBlocking
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.junit.Test
import service.cashpool.CashpoolService
import domain.commands.CreateCashpoolCommand
import service.cashpool_member.CashpoolMemberService
import domain.commands.CreateCashpoolMemberCommand
import domain.commands.CreateUserCommand
import service.user.UserService
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.time.Clock

import core.exceptions.CashpoolMemberNotFound
import domain.repositories.CashpoolMemberRepositoryImpl
import domain.repositories.CashpoolRepositoryImpl
import domain.repositories.UserRepositoryImpl
import core.exceptions.CashpoolNotFound
import core.exceptions.Conflict
import core.exceptions.UserNotFound
import kotlinx.datetime.todayIn
import testutils.Commands
import kotlin.test.assertFailsWith

class CashpoolMemberServiceTest : BaseServiceTest() {
    private val userRepo = UserRepositoryImpl()
    private val userService = UserService(userRepo)
    private val cashpoolRepo = CashpoolRepositoryImpl()
    private val cashpoolService = CashpoolService(userService, cashpoolRepo)
    private val cashpoolMemberRepo = CashpoolMemberRepositoryImpl()
    private val cashpoolMemberService = CashpoolMemberService(cashpoolMemberRepo, userRepo, cashpoolRepo)

    private suspend fun createTestCashpool(ownerId: Int): Int {
        return cashpoolService.create(CreateCashpoolCommand("Title", "Desc", ownerId)).id
    }

    @Test
    fun `create member - success`() {
        runBlocking {
            val userId = userService.create(Commands.User.create()).id
            val cashpoolId = createTestCashpool(userId)
            val cmd = CreateCashpoolMemberCommand(userId, cashpoolId)

            val member = cashpoolMemberService.create(cmd)

            assertNotNull(member)
            assertEquals(userId, member.user.id)
            assertEquals(cashpoolId, member.cashpool.id)
        }
    }

    @Test
    fun `create member - user not found - fails`() {
        runBlocking {
            val userId = userService.create(Commands.User.create()).id
            val cashpoolId = createTestCashpool(userId)
            assertFailsWith<UserNotFound> {
                cashpoolMemberService.create(CreateCashpoolMemberCommand(999, cashpoolId))
            }
        }
    }

    @Test
    fun `create member - cashpool not found - fails`() {
        runBlocking {
            val userId = userService.create(Commands.User.create()).id
            assertFailsWith<CashpoolNotFound> {
                cashpoolMemberService.create(CreateCashpoolMemberCommand(userId, 999))
            }
        }
    }

    @Test
    fun `create member - conflict already a member - fails`() {
        runBlocking {
            val userId = userService.create(Commands.User.create()).id
            val cashpoolId = createTestCashpool(userId)

            cashpoolMemberService.create(CreateCashpoolMemberCommand(userId, cashpoolId))
            assertFailsWith<Conflict> {
                cashpoolMemberService.create(CreateCashpoolMemberCommand(userId, cashpoolId))
            }
        }
    }

    @Test
    fun `findByCashpoolId - returns all members`() {
        runBlocking {
            val u1 = userService.create(Commands.User.create(email = "u1@ex.com")).id
            val u2 = userService.create(Commands.User.create(email = "u2@ex.com")).id
            val cp = createTestCashpool(u1)
            cashpoolMemberService.create(CreateCashpoolMemberCommand(u1, cp))
            cashpoolMemberService.create(CreateCashpoolMemberCommand(u2, cp))

            val members = cashpoolMemberService.findByCashpoolId(cp)
            assertEquals(2, members.size)
        }
    }

    @Test
    fun `findById - exists - returns member`() {
        runBlocking {
            val userId = userService.create(Commands.User.create()).id
            val cashpoolId = createTestCashpool(userId)
            val member = cashpoolMemberService.create(CreateCashpoolMemberCommand(userId, cashpoolId))

            val found = cashpoolMemberService.findById(member.id)

            assertNotNull(found)
            assertEquals(member.id, found.id)
        }
    }

    @Test
    fun `findById - not found - throws exception`() {
        runBlocking {
            assertFailsWith<CashpoolMemberNotFound> {
                cashpoolMemberService.findById(999)
            }
        }
    }

    @Test
    fun `findAll - returns all`() {
        runBlocking {
            val userId = userService.create(Commands.User.create()).id
            val cashpoolId = createTestCashpool(userId)
            cashpoolMemberService.create(CreateCashpoolMemberCommand(userId, cashpoolId))

            val all = cashpoolMemberService.findAll()
            assert(all.isNotEmpty())
        }
    }
}
