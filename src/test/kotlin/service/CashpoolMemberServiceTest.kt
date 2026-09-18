package service

import core.exceptions.CashpoolMemberNotFound
import core.exceptions.CashpoolNotFound
import core.exceptions.Conflict
import core.exceptions.UserNotFound
import domain.commands.CreateCashpoolCommand
import domain.commands.CreateCashpoolMemberCommand
import domain.repositories.CashpoolMemberRepositoryImpl
import domain.repositories.CashpoolRepositoryImpl
import domain.repositories.UserRepositoryImpl
import kotlinx.coroutines.runBlocking
import org.junit.Test
import service.cashpool.CashpoolService
import service.cashpool_member.CashpoolMemberService
import service.user.UserService
import testutils.Commands
import testutils.Contexts
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull

class CashpoolMemberServiceTest : BaseServiceTest() {
    private val userRepo = UserRepositoryImpl()
    private val userService = UserService(userRepo)
    private val cashpoolRepo = CashpoolRepositoryImpl()
    private val cashpoolService = CashpoolService(userService, cashpoolRepo)
    private val cashpoolMemberRepo = CashpoolMemberRepositoryImpl()
    private val cashpoolMemberService = CashpoolMemberService(cashpoolMemberRepo, userRepo, cashpoolRepo)

    private suspend fun createTestCashpool(ownerId: Int): Int {
        return context(Contexts.of(ownerId)) {
            cashpoolService.create(CreateCashpoolCommand("Title", "Desc", ownerId)).id
        }
    }

    @Test
    fun `create member - success`() {
        runBlocking {
            val user = userService.create(Commands.User.create())
            val cashpoolId = createTestCashpool(user.id)
            context(Contexts.of(user)) {
                val cmd = CreateCashpoolMemberCommand(user.id, cashpoolId)

                val member = cashpoolMemberService.create(cmd)

                assertNotNull(member)
                assertEquals(user.id, member.user.id)
                assertEquals(cashpoolId, member.cashpool.id)
            }
        }
    }

    @Test
    fun `create member - user not found - fails`() {
        runBlocking {
            val user = userService.create(Commands.User.create())
            val cashpoolId = createTestCashpool(user.id)
            assertFailsWith<UserNotFound> {
                context(Contexts.default) {
                    cashpoolMemberService.create(CreateCashpoolMemberCommand(999, cashpoolId))
                }
            }
        }
    }

    @Test
    fun `create member - cashpool not found - fails`() {
        runBlocking {
            val user = userService.create(Commands.User.create())
            assertFailsWith<CashpoolNotFound> {
                context(Contexts.of(user)) {
                    cashpoolMemberService.create(CreateCashpoolMemberCommand(user.id, 999))
                }
            }
        }
    }

    @Test
    fun `create member - conflict already a member - fails`() {
        runBlocking {
            val user = userService.create(Commands.User.create())
            val cashpoolId = createTestCashpool(user.id)

            context(Contexts.of(user)) {
                cashpoolMemberService.create(CreateCashpoolMemberCommand(user.id, cashpoolId))
                assertFailsWith<Conflict> {
                    cashpoolMemberService.create(CreateCashpoolMemberCommand(user.id, cashpoolId))
                }
            }
        }
    }

    @Test
    fun `findByCashpoolId - returns all members`() {
        runBlocking {
            val u1 = userService.create(Commands.User.create(email = "u1@ex.com"))
            val u2 = userService.create(Commands.User.create(email = "u2@ex.com"))
            val cp = createTestCashpool(u1.id)
            context(Contexts.of(u1)) {
                cashpoolMemberService.create(CreateCashpoolMemberCommand(u1.id, cp))
            }
            context(Contexts.of(u2)) {
                cashpoolMemberService.create(CreateCashpoolMemberCommand(u2.id, cp))

                val members = cashpoolMemberService.findByCashpoolId(cp)
                assertEquals(2, members.size)
            }
        }
    }

    @Test
    fun `findById - exists - returns member`() {
        runBlocking {
            val user = userService.create(Commands.User.create())
            val cashpoolId = createTestCashpool(user.id)
            context(Contexts.of(user)) {
                val member = cashpoolMemberService.create(CreateCashpoolMemberCommand(user.id, cashpoolId))

                val found = cashpoolMemberService.findById(member.id)

                assertNotNull(found)
                assertEquals(member.id, found.id)
            }
        }
    }

    @Test
    fun `findById - not found - throws exception`() {
        runBlocking {
            assertFailsWith<CashpoolMemberNotFound> {
                context(Contexts.default) {
                    cashpoolMemberService.findById(999)
                }
            }
        }
    }

    @Test
    fun `findAll - returns all`() {
        runBlocking {
            val user = userService.create(Commands.User.create())
            val cashpoolId = createTestCashpool(user.id)
            context(Contexts.of(user)) {
                cashpoolMemberService.create(CreateCashpoolMemberCommand(user.id, cashpoolId))

                val all = cashpoolMemberService.findAll()
                assert(all.isNotEmpty())
            }
        }
    }
}
