package domain.policies

import domain.commands.CreateCashpoolSettlementCommand
import io.mockk.every
import io.mockk.mockk
import org.junit.Test
import testutils.Contexts
import testutils.Users
import kotlin.test.assertFails

class CashpoolSettlementPolicyTest {
    private val currentUser = Users.nonAdminUser
    private val otherUser = Users.nonAdminUser.copy(id = currentUser.id + 1)
    private val adminUser = Users.nonAdminUser.copy(isAdmin = true)

    @Test
    fun `canCreate - admin override`() {
        val cmd = mockk<CreateCashpoolSettlementCommand>()
        context(Contexts.of(adminUser)) {
            CashpoolSettlementPolicy.canCreate(cmd, isFromMember = false, isToMember = false)
        }
    }

    @Test
    fun `canCreate - valid members and matching sender`() {
        val cmd = mockk<CreateCashpoolSettlementCommand> {
            every { fromId } returns currentUser.id
            every { toId } returns otherUser.id
        }
        context(Contexts.of(currentUser)) {
            CashpoolSettlementPolicy.canCreate(cmd, isFromMember = true, isToMember = true)
        }
    }

    @Test
    fun `canCreate - fromId is not member fails`() {
        val cmd = mockk<CreateCashpoolSettlementCommand> {
            every { fromId } returns currentUser.id
        }
        context(Contexts.of(currentUser)) {
            assertFails { CashpoolSettlementPolicy.canCreate(cmd, isFromMember = false, isToMember = true) }
        }
    }

    @Test
    fun `canCreate - toId is not member fails`() {
        val cmd = mockk<CreateCashpoolSettlementCommand> {
            every { fromId } returns currentUser.id
            every { toId } returns otherUser.id
        }
        context(Contexts.of(currentUser)) {
            assertFails { CashpoolSettlementPolicy.canCreate(cmd, isFromMember = true, isToMember = false) }
        }
    }

    @Test
    fun `canCreate - sender mismatch fails`() {
        val cmd = mockk<CreateCashpoolSettlementCommand> {
            every { fromId } returns otherUser.id
        }
        context(Contexts.of(currentUser)) {
            assertFails { CashpoolSettlementPolicy.canCreate(cmd, isFromMember = true, isToMember = true) }
        }
    }

    @Test
    fun `canView - admin override`() {
        context(Contexts.of(adminUser)) {
            CashpoolSettlementPolicy.canView(isMember = false)
        }
    }

    @Test
    fun `canView - member`() {
        context(Contexts.of(currentUser)) {
            CashpoolSettlementPolicy.canView(isMember = true)
        }
    }

    @Test
    fun `canView - not member fails`() {
        context(Contexts.of(currentUser)) {
            assertFails { CashpoolSettlementPolicy.canView(isMember = false) }
        }
    }
}