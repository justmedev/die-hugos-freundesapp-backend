package service

import core.exceptions.NotaCashpoolMember
import io.ktor.server.plugins.*
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.junit.Test
import service.cashpool.CashpoolService
import domain.commands.CreateCashpoolCommand
import domain.commands.CreateCashpoolMemberCommand
import domain.commands.CreateUserCommand
import service.user.UserService
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.time.Clock

import core.exceptions.CashpoolNotFound
import core.exceptions.UserNotFound
import domain.repositories.CashpoolRepositoryImpl
import domain.repositories.UserRepositoryImpl
import testutils.Commands

class CashpoolServiceTest : BaseServiceTest() {
    private val userRepo = UserRepositoryImpl()
    private val userService = UserService(userRepo)
    private val cashpoolRepo = CashpoolRepositoryImpl()
    private val cashpoolService = CashpoolService(userService, cashpoolRepo)

    @Test
    fun `create cashpool - success`() {
        runBlocking {
            val userId = userService.create(Commands.User.create()).id
            val cmd = CreateCashpoolCommand("Title", "Desc", userId)

            val cashpool = cashpoolService.create(cmd)

            assertNotNull(cashpool)
            assertEquals("Title", cashpool.title)
            assertEquals(userId, cashpool.owner.id)
        }
    }

    @Test
    fun `create cashpool - user not found - fails`() {
        runBlocking {
            val cmd = CreateCashpoolCommand("Title", "Desc", 999)
            assertFailsWith<UserNotFound> {
                cashpoolService.create(cmd)
            }
        }
    }

    @Test
    fun `findByIdOnlyIfMember - is member - success`() {
        runBlocking {
            val userId = userService.create(Commands.User.create()).id
            val cashpool = cashpoolService.create(CreateCashpoolCommand("Title", "Desc", userId))

            val cashpoolMemberRepo = domain.repositories.CashpoolMemberRepositoryImpl()
            val cashpoolMemberService = service.cashpool_member.CashpoolMemberService(cashpoolMemberRepo, userRepo, cashpoolRepo)
            cashpoolMemberService.create(CreateCashpoolMemberCommand(userId, cashpool.id))

            val found = cashpoolService.findByIdOnlyIfMember(cashpool.id, userId)
            assertNotNull(found)
            assertEquals(cashpool.id, found.id)
        }
    }

    @Test
    fun `findByIdOnlyIfMember - not member - fails`() {
        runBlocking {
            val ownerId = userService.create(Commands.User.create(email = "owner@ex.com")).id
            val otherId = userService.create(Commands.User.create(email = "other@ex.com")).id
            val cashpool = cashpoolService.create(CreateCashpoolCommand("Title", "Desc", ownerId))

            assertFailsWith<NotaCashpoolMember> {
                cashpoolService.findByIdOnlyIfMember(cashpool.id, otherId)
            }
        }
    }

    @Test
    fun `findById - non-existing - fails`() {
        runBlocking {
            assertFailsWith<CashpoolNotFound> {
                cashpoolService.findById(999)
            }
        }
    }

    @Test
    fun `findAll - returns all cashpools`() {
        runBlocking {
            val userId = userService.create(Commands.User.create()).id
            cashpoolService.create(CreateCashpoolCommand("T1", "D1", userId))
            cashpoolService.create(CreateCashpoolCommand("T2", "D2", userId))

            val all = cashpoolService.findAll()
            assertEquals(2, all.size)
        }
    }
}
