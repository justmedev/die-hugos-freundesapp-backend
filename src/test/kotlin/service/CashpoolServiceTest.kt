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
            val userId = userService.create(Commands.User.create()).id
            val cmd = CreateCashpoolCommand("Title", "Desc", userId)

            val cashpool = cashpoolService.create(cmd)

            assertNotNull(cashpool)
            assertEquals("Title", cashpool.title)
            assertEquals(userId, cashpool.owner.id)
        }
    }

    @Test
    fun `update cashpool - owner - success`() {
        runBlocking {
            val userId = userService.create(Commands.User.create()).id
            val cashpool = cashpoolService.create(CreateCashpoolCommand("Title", "Desc", userId))

            // Need to be a member to update (owner is usually a member, but we need to ensure it for findByIdOnlyIfMember)
            val cashpoolMemberRepo = CashpoolMemberRepositoryImpl()
            val cashpoolMemberService =
                CashpoolMemberService(cashpoolMemberRepo, userRepo, cashpoolRepo)
            cashpoolMemberService.create(CreateCashpoolMemberCommand(userId, cashpool.id))

            val cmd = UpdateCashpoolCommand(cashpool.id, UpdateProperty("New Title"), UpdateProperty("New Desc"))
            val updated = cashpoolService.update(userId, cmd)

            assertEquals("New Title", updated.title)
            assertEquals("New Desc", updated.description)
        }
    }

    @Test
    fun `update cashpool - partial update - success`() {
        runBlocking {
            val userId = userService.create(Commands.User.create()).id
            val cashpool = cashpoolService.create(CreateCashpoolCommand("Original Title", "Original Desc", userId))
            cashpoolMemberService.create(CreateCashpoolMemberCommand(userId, cashpool.id))

            val cmd = UpdateCashpoolCommand(cashpool.id, title = UpdateProperty("Updated Title"))
            val updated = cashpoolService.update(userId, cmd)

            assertEquals("Updated Title", updated.title)
            assertEquals("Original Desc", updated.description)
        }
    }

    @Test
    fun `update cashpool - not owner - fails`() {
        runBlocking {
            val ownerId = userService.create(Commands.User.create(email = "owner@ex.com")).id
            val otherId = userService.create(Commands.User.create(email = "other@ex.com")).id
            val cashpool = cashpoolService.create(CreateCashpoolCommand("Title", "Desc", ownerId))

            // Other user is a member but NOT owner
            cashpoolMemberService.create(CreateCashpoolMemberCommand(otherId, cashpool.id))

            val cmd = UpdateCashpoolCommand(cashpool.id, UpdateProperty("New Title"), UpdateProperty("New Desc"))
            assertFailsWith<NotCashpoolOwner> {
                cashpoolService.update(otherId, cmd)
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
                cashpoolService.create(cmd)
            }
        }
    }

    @Test
    fun `findByIdOnlyIfMember - is member - success`() {
        runBlocking {
            val userId = userService.create(Commands.User.create()).id
            val cashpool = cashpoolService.create(CreateCashpoolCommand("Title", "Desc", userId))
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

    @Test
    fun `deleteById - non-existing - fails`() {
        runBlocking {
            val userId = userService.create(Commands.User.create()).id
            assertFailsWith<CashpoolNotFound> { cashpoolService.deleteById(1, userId) }
        }
    }

    @Test
    fun `deleteById - not a member - fails`() {
        runBlocking {
            val notAMemberId = userService.create(Commands.User.create()).id
            val ownerId = userService.create(Commands.User.create()).id
            val cashpool = cashpoolService.create(CreateCashpoolCommand("Title", "Desc", ownerId))
            cashpoolMemberService.create(CreateCashpoolMemberCommand(ownerId, cashpool.id))

            assertFailsWith<NotaCashpoolMember> { cashpoolService.deleteById(cashpool.id, notAMemberId) }
        }
    }

    @Test
    fun `deleteById - not owner - fails`() {
        runBlocking {
            val notOwnerId = userService.create(Commands.User.create()).id
            val ownerId = userService.create(Commands.User.create()).id
            val cashpool = cashpoolService.create(CreateCashpoolCommand("Title", "Desc", ownerId))
            cashpoolMemberService.create(CreateCashpoolMemberCommand(ownerId, cashpool.id))
            cashpoolMemberService.create(CreateCashpoolMemberCommand(notOwnerId, cashpool.id))

            assertFailsWith<NotCashpoolOwner> { cashpoolService.deleteById(cashpool.id, notOwnerId) }
        }
    }

    @Test
    fun `deleteById - success`() {
        runBlocking {
            val userId = userService.create(Commands.User.create()).id
            val cashpool = cashpoolService.create(CreateCashpoolCommand("Title", "Desc", userId))
            cashpoolMemberService.create(CreateCashpoolMemberCommand(userId, cashpool.id))

            cashpoolService.deleteById(cashpool.id, userId)

            assertFailsWith<CashpoolNotFound> {
                cashpoolService.findById(cashpool.id)
            }
        }
    }
}
