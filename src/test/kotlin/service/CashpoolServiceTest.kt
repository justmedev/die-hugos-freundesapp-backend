package service

import core.exceptions.CashpoolNotFound
import core.exceptions.NotCashpoolOwner
import core.exceptions.NotaCashpoolMember
import core.exceptions.UserNotFound
import core.utils.UpdateProperty
import domain.commands.CreateCashpoolCommand
import domain.commands.CreateCashpoolMemberCommand
import domain.commands.UpdateCashpoolCommand
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

class CashpoolServiceTest : BaseServiceTest() {
    private val userRepo = UserRepositoryImpl()
    private val userService = UserService(userRepo)
    private val cashpoolRepo = CashpoolRepositoryImpl()
    private val cashpoolService = CashpoolService(userService, cashpoolRepo)
    private val cashpoolMemberRepo = CashpoolMemberRepositoryImpl()
    private val cashpoolMemberService = CashpoolMemberService(cashpoolMemberRepo, userRepo, cashpoolRepo)

    @Test
    fun `create cashpool - success`() {
        runBlocking {
            val user = userService.create(Commands.User.create())
            context(Contexts.of(user)) {
                val cmd = CreateCashpoolCommand("Title", "Desc", user.id)

                val cashpool = cashpoolService.create(cmd)

                assertNotNull(cashpool)
                assertEquals("Title", cashpool.title)
                assertEquals(user.id, cashpool.owner.id)
            }
        }
    }

    @Test
    fun `update cashpool - owner - success`() {
        runBlocking {
            val user = userService.create(Commands.User.create())
            context(Contexts.of(user)) {
                val cashpool = cashpoolService.create(CreateCashpoolCommand("Title", "Desc", user.id))
                cashpoolMemberService.create(CreateCashpoolMemberCommand(user.id, cashpool.id))

                val cmd = UpdateCashpoolCommand(cashpool.id, UpdateProperty("New Title"), UpdateProperty("New Desc"))
                val updated = cashpoolService.update(cmd)

                assertEquals("New Title", updated.title)
                assertEquals("New Desc", updated.description)
            }
        }
    }

    @Test
    fun `update cashpool - partial update - success`() {
        runBlocking {
            val user = userService.create(Commands.User.create())
            context(Contexts.of(user)) {
                val cashpool = cashpoolService.create(CreateCashpoolCommand("Original Title", "Original Desc", user.id))
                cashpoolMemberService.create(CreateCashpoolMemberCommand(user.id, cashpool.id))

                val cmd = UpdateCashpoolCommand(cashpool.id, title = UpdateProperty("Updated Title"))
                val updated = cashpoolService.update(cmd)

                assertEquals("Updated Title", updated.title)
                assertEquals("Original Desc", updated.description)
            }
        }
    }

    @Test
    fun `update cashpool - not owner - fails`() {
        runBlocking {
            val owner = userService.create(Commands.User.create(email = "owner@ex.com"))
            val other = userService.create(Commands.User.create(email = "other@ex.com"))
            val cashpool = context(Contexts.of(owner)) { cashpoolService.create(CreateCashpoolCommand("Title", "Desc", owner.id)) }

            context(Contexts.of(other)) {
                cashpoolMemberService.create(CreateCashpoolMemberCommand(other.id, cashpool.id))

                val cmd = UpdateCashpoolCommand(cashpool.id, UpdateProperty("New Title"), UpdateProperty("New Desc"))
                assertFailsWith<NotCashpoolOwner> {
                    cashpoolService.update(cmd)
                }
            }
        }
    }

    @Test
    fun `update cashpool - invalid command - fails`() {
        runBlocking {
            assertFailsWith<IllegalArgumentException> {
                UpdateCashpoolCommand(1, UpdateProperty(""), UpdateProperty(""))
            }
        }
    }

    @Test
    fun `create cashpool - user not found - fails`() {
        runBlocking {
            val cmd = CreateCashpoolCommand("Title", "Desc", 999)
            assertFailsWith<UserNotFound> {
                context(Contexts.default) { cashpoolService.create(cmd) }
            }
        }
    }

    @Test
    fun `findById - non-existing - fails`() {
        runBlocking {
            assertFailsWith<CashpoolNotFound> {
                context(Contexts.default) { cashpoolService.findById(999) }
            }
        }
    }

    @Test
    fun `findAll - returns all cashpools`() {
        runBlocking {
            val user = userService.create(Commands.User.create())
            context(Contexts.of(user)) {
                val cp1 = cashpoolService.create(CreateCashpoolCommand("T1", "D1", user.id))
                val cp2 = cashpoolService.create(CreateCashpoolCommand("T2", "D2", user.id))
                cashpoolMemberService.create(CreateCashpoolMemberCommand(user.id, cp1.id))
                cashpoolMemberService.create(CreateCashpoolMemberCommand(user.id, cp2.id))

                val all = cashpoolService.findAll()
                assertEquals(2, all.size)
            }
        }
    }

    @Test
    fun `deleteById - non-existing - fails`() {
        runBlocking {
            val user = userService.create(Commands.User.create())
            context(Contexts.of(user)) {
                assertFailsWith<CashpoolNotFound> { cashpoolService.deleteById(1, user.id) }
            }
        }
    }

    @Test
    fun `deleteById - not a member - fails`() {
        runBlocking {
            val notAMember = userService.create(Commands.User.create())
            val owner = userService.create(Commands.User.create())
            val cashpool = context(Contexts.of(owner)) {
                val cp = cashpoolService.create(CreateCashpoolCommand("Title", "Desc", owner.id))
                cashpoolMemberService.create(CreateCashpoolMemberCommand(owner.id, cp.id))
                cp
            }

            context(Contexts.of(notAMember)) {
                assertFailsWith<NotaCashpoolMember> { cashpoolService.deleteById(cashpool.id, notAMember.id) }
            }
        }
    }

    @Test
    fun `deleteById - not owner - fails`() {
        runBlocking {
            val notOwner = userService.create(Commands.User.create())
            val owner = userService.create(Commands.User.create())
            val cashpool = context(Contexts.of(owner)) {
                val cp = cashpoolService.create(CreateCashpoolCommand("Title", "Desc", owner.id))
                cashpoolMemberService.create(CreateCashpoolMemberCommand(owner.id, cp.id))
                cp
            }
            context(Contexts.of(notOwner)) {
                cashpoolMemberService.create(CreateCashpoolMemberCommand(notOwner.id, cashpool.id))
                assertFailsWith<NotCashpoolOwner> { cashpoolService.deleteById(cashpool.id, notOwner.id) }
            }
        }
    }

    @Test
    fun `deleteById - success`() {
        runBlocking {
            val user = userService.create(Commands.User.create())
            context(Contexts.of(user)) {
                val cashpool = cashpoolService.create(CreateCashpoolCommand("Title", "Desc", user.id))
                cashpoolMemberService.create(CreateCashpoolMemberCommand(user.id, cashpool.id))

                cashpoolService.deleteById(cashpool.id, user.id)

                assertFailsWith<CashpoolNotFound> {
                    cashpoolService.findById(cashpool.id)
                }
            }
        }
    }
}
