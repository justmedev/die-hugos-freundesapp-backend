package domain.policies

import domain.commands.CreateCashpoolMemberCommand
import io.mockk.every
import io.mockk.mockk
import testutils.CashpoolMembers
import testutils.Contexts
import testutils.Users
import kotlin.test.Test
import kotlin.test.assertFails

class CashpoolMemberPolicyTest {
    private val currentUser = Users.nonAdminUser
    private val otherUser = Users.nonAdminUser.copy(id = currentUser.id + 1)

    @Test
    fun `canCreate - same user`() {
        val cmd = mockk<CreateCashpoolMemberCommand> {
            every { userId } returns currentUser.id
        }
        context(Contexts.of(currentUser)) {
            CashpoolMemberPolicy.canCreate(cmd)
        }
    }

    @Test
    fun `canCreate - different user fails`() {
        val cmd = mockk<CreateCashpoolMemberCommand> {
            every { userId } returns otherUser.id
        }
        context(Contexts.of(currentUser)) {
            assertFails { CashpoolMemberPolicy.canCreate(cmd) }
        }
    }

    @Test
    fun `canCreate - internal bypasses`() {
        val cmd = mockk<CreateCashpoolMemberCommand>()
        context(Contexts.internal) {
            CashpoolMemberPolicy.canCreate(cmd)
        }
    }

    @Test
    fun `canView - null member`() {
        context(Contexts.internal) {
            CashpoolMemberPolicy.canView(null)
        }
    }

    @Test
    fun `canView - self member`() {
        val member = CashpoolMembers.of(currentUser)
        context(Contexts.of(currentUser)) {
            CashpoolMemberPolicy.canView(member)
        }
    }

    @Test
    fun `canView - other member fails`() {
        val member = CashpoolMembers.of(otherUser)
        context(Contexts.of(currentUser)) {
            assertFails { CashpoolMemberPolicy.canView(member) }
        }
    }

    @Test
    fun `canView - internal bypasses`() {
        val member = CashpoolMembers.of(otherUser)
        context(Contexts.internal) {
            CashpoolMemberPolicy.canView(member)
        }
    }

    @Test
    fun `canView - null member should throw`() {
        context(Contexts.of(currentUser)) {
            // Null member passes without throwing Forbidden
            assertFails { CashpoolMemberPolicy.canView(member = null) }
        }
    }
}