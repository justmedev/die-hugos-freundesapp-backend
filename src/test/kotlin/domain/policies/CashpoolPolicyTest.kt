package domain.policies

import domain.commands.CreateCashpoolCommand
import domain.models.Cashpool
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.junit.Test
import testutils.Contexts
import testutils.Users
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.time.Clock

class CashpoolPolicyTest {
    private val now = Clock.System.now().toLocalDateTime(TimeZone.UTC)
    private val ownerUser = Users.nonAdminUser
    private val otherUser = Users.nonAdminUser
    private val adminUser = Users.nonAdminUser.copy(isAdmin = true)

    private val cashpool = Cashpool(
        id = 1,
        title = "Test Pool",
        description = "Test Desc",
        owner = ownerUser,
        isOpened = true,
        createdAt = now
    )

    @Test
    fun `canCreate - admin override`() {
        context(Contexts.of(adminUser)) {
            val cmd = CreateCashpoolCommand("Title", "Desc", ownerUser.id)
            assertTrue(CashpoolPolicy.canCreate(cmd))
        }
    }

    @Test
    fun `canCreate - owner user`() {
        context(Contexts.of(ownerUser)) {
            val cmd = CreateCashpoolCommand("Title", "Desc", ownerUser.id)
            assertTrue(CashpoolPolicy.canCreate(cmd))
        }
    }

    @Test
    fun `canCreate - different user fails`() {
        context(Contexts.of(otherUser)) {
            val cmd = CreateCashpoolCommand("Title", "Desc", ownerUser.id)
            assertFalse(CashpoolPolicy.canCreate(cmd))
        }
    }

    @Test
    fun `canView - admin override`() {
        context(Contexts.of(adminUser)) {
            assertTrue(CashpoolPolicy.canView(isMember = false))
        }
    }

    @Test
    fun `canView - is member`() {
        context(Contexts.of(otherUser)) {
            assertTrue(CashpoolPolicy.canView(isMember = true))
        }
    }

    @Test
    fun `canView - not member fails`() {
        context(Contexts.of(otherUser)) {
            assertFalse(CashpoolPolicy.canView(isMember = false))
        }
    }

    @Test
    fun `canUpdate - admin override`() {
        context(Contexts.of(adminUser)) {
            assertTrue(CashpoolPolicy.canUpdate(cashpool, isMember = false))
        }
    }

    @Test
    fun `canUpdate - owner and member`() {
        context(Contexts.of(ownerUser)) {
            assertTrue(CashpoolPolicy.canUpdate(cashpool, isMember = true))
        }
    }

    @Test
    fun `canUpdate - member but not owner fails`() {
        context(Contexts.of(otherUser)) {
            assertFalse(CashpoolPolicy.canUpdate(cashpool, isMember = true))
        }
    }

    @Test
    fun `canDelete - admin override`() {
        context(Contexts.of(adminUser)) {
            assertTrue(CashpoolPolicy.canDelete(cashpool, isMember = false))
        }
    }

    @Test
    fun `canDelete - owner and member`() {
        context(Contexts.of(ownerUser)) {
            assertTrue(CashpoolPolicy.canDelete(cashpool, isMember = true))
        }
    }

    @Test
    fun `canDelete - member but not owner fails`() {
        context(Contexts.of(otherUser)) {
            assertFalse(CashpoolPolicy.canDelete(cashpool, isMember = true))
        }
    }
}
