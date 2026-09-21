package domain.policies

import org.junit.Test
import testutils.Contexts
import testutils.Users
import kotlin.test.assertFails

class CashpoolSuggestedSettlementPolicyTest {
    private val currentUser = Users.nonAdminUser
    private val otherUser = Users.nonAdminUser.copy(id = currentUser.id + 1)
    private val adminUser = Users.nonAdminUser.copy(isAdmin = true)

    @Test
    fun `canCalculateSettlement - admin override`() {
        context(Contexts.of(adminUser)) {
            CashpoolSuggestedSettlementPolicy.canCalculateSettlement(isMember = false)
        }
    }

    @Test
    fun `canCalculateSettlement - member`() {
        context(Contexts.of(currentUser)) {
            CashpoolSuggestedSettlementPolicy.canCalculateSettlement(isMember = true)
        }
    }

    @Test
    fun `canCalculateSettlement - not member fails`() {
        context(Contexts.of(currentUser)) {
            assertFails { CashpoolSuggestedSettlementPolicy.canCalculateSettlement(isMember = false) }
        }
    }

    @Test
    fun `canCalculateUserSettlementSummary - admin override`() {
        context(Contexts.of(adminUser)) {
            CashpoolSuggestedSettlementPolicy.canCalculateUserSettlementSummary(forUserId = otherUser.id)
        }
    }

    @Test
    fun `canCalculateUserSettlementSummary - self`() {
        context(Contexts.of(currentUser)) {
            CashpoolSuggestedSettlementPolicy.canCalculateUserSettlementSummary(forUserId = currentUser.id)
        }
    }

    @Test
    fun `canCalculateUserSettlementSummary - other user fails`() {
        context(Contexts.of(currentUser)) {
            assertFails { CashpoolSuggestedSettlementPolicy.canCalculateUserSettlementSummary(forUserId = otherUser.id) }
        }
    }
}