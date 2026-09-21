package domain.policies

import domain.commands.CreateCashpoolTransactionCommand
import domain.commands.UpdateCashpoolTransactionCommand
import io.mockk.every
import io.mockk.mockk
import org.junit.Test
import testutils.CashpoolTransactions
import testutils.Contexts
import testutils.Users
import kotlin.test.assertFails

class CashpoolTransactionPolicyTest {
    private val currentUser = Users.nonAdminUser
    private val otherUser = Users.nonAdminUser.copy(id = currentUser.id + 1)
    private val adminUser = Users.nonAdminUser.copy(isAdmin = true)
    private val transaction = CashpoolTransactions.of(owner = currentUser)

    @Test
    fun `canCreate - admin override`() {
        val cmd = mockk<CreateCashpoolTransactionCommand>()
        context(Contexts.of(adminUser)) {
            CashpoolTransactionPolicy.canCreate(cmd, cpExists = true, isOpened = true, isMember = true)
        }
    }

    @Test
    fun `canCreate - owner user`() {
        val cmd = mockk<CreateCashpoolTransactionCommand> {
            every { ownerId } returns currentUser.id
        }
        context(Contexts.of(currentUser)) {
            CashpoolTransactionPolicy.canCreate(cmd, cpExists = true, isOpened = true, isMember = true)
        }
    }

    @Test
    fun `canCreate - cashpool not found fails first`() {
        val cmd = mockk<CreateCashpoolTransactionCommand>()
        context(Contexts.of(adminUser)) {
            assertFails { CashpoolTransactionPolicy.canCreate(cmd, cpExists = false, isOpened = true, isMember = true) }
        }
    }

    @Test
    fun `canCreate - cashpool closed fails before admin check`() {
        val cmd = mockk<CreateCashpoolTransactionCommand>()
        context(Contexts.of(adminUser)) {
            assertFails { CashpoolTransactionPolicy.canCreate(cmd, cpExists = true, isOpened = false, isMember = true) }
        }
    }

    @Test
    fun `canCreate - not member fails before admin check`() {
        val cmd = mockk<CreateCashpoolTransactionCommand>()
        context(Contexts.of(adminUser)) {
            assertFails { CashpoolTransactionPolicy.canCreate(cmd, cpExists = true, isOpened = true, isMember = false) }
        }
    }

    @Test
    fun `canCreate - different user fails`() {
        val cmd = mockk<CreateCashpoolTransactionCommand> {
            every { ownerId } returns otherUser.id
        }
        context(Contexts.of(currentUser)) {
            assertFails { CashpoolTransactionPolicy.canCreate(cmd, cpExists = true, isOpened = true, isMember = true) }
        }
    }

    @Test
    fun `canCreate - admin override should fail when cashpool not found`() {
        val cmd = mockk<CreateCashpoolTransactionCommand>()
        context(Contexts.of(adminUser)) {
            assertFails {
                CashpoolTransactionPolicy.canCreate(cmd, cpExists = false, isOpened = true, isMember = false)
            }
        }
    }

    @Test
    fun `canCreate - admin override should fail when cashpool closed`() {
        val cmd = mockk<CreateCashpoolTransactionCommand>()
        context(Contexts.of(adminUser)) {
            assertFails {
                CashpoolTransactionPolicy.canCreate(cmd, cpExists = true, isOpened = false, isMember = false)
            }
        }
    }

    @Test
    fun `canView - admin override`() {
        context(Contexts.of(adminUser)) {
            CashpoolTransactionPolicy.canView(isMember = false)
        }
    }

    @Test
    fun `canView - member`() {
        context(Contexts.of(currentUser)) {
            CashpoolTransactionPolicy.canView(isMember = true)
        }
    }

    @Test
    fun `canView - not member fails`() {
        context(Contexts.of(currentUser)) {
            assertFails { CashpoolTransactionPolicy.canView(isMember = false) }
        }
    }

    @Test
    fun `canUpdate - admin override`() {
        val cmd = mockk<UpdateCashpoolTransactionCommand>()
        context(Contexts.of(adminUser)) {
            CashpoolTransactionPolicy.canUpdate(cmd, isOpened = true, isMember = true, transaction = transaction)
        }
    }

    @Test
    fun `canUpdate - owner`() {
        val cmd = mockk<UpdateCashpoolTransactionCommand>()
        context(Contexts.of(currentUser)) {
            CashpoolTransactionPolicy.canUpdate(cmd, isOpened = true, isMember = true, transaction = transaction)
        }
    }

    @Test
    fun `canUpdate - not member fails before admin check`() {
        val cmd = mockk<UpdateCashpoolTransactionCommand>()
        context(Contexts.of(adminUser)) {
            assertFails {
                CashpoolTransactionPolicy.canUpdate(
                    cmd,
                    isOpened = true,
                    isMember = false,
                    transaction = transaction
                )
            }
        }
    }

    @Test
    fun `canUpdate - non-owner fails`() {
        val cmd = mockk<UpdateCashpoolTransactionCommand>()
        context(Contexts.of(otherUser)) {
            assertFails {
                CashpoolTransactionPolicy.canUpdate(
                    cmd,
                    isOpened = true,
                    isMember = true,
                    transaction = transaction
                )
            }
        }
    }

    @Test
    fun `canUpdate - admin override should fail when admin is not a member`() {
        val cmd = mockk<UpdateCashpoolTransactionCommand>()
        val tx = CashpoolTransactions.of(owner = otherUser)
        context(Contexts.of(adminUser)) {
            assertFails {
                CashpoolTransactionPolicy.canUpdate(cmd, isOpened = true, isMember = false, transaction = tx)
            }
        }
    }

    @Test
    fun `canUpdate - admin override should fail when cashpool is closed`() {
        val cmd = mockk<UpdateCashpoolTransactionCommand>()
        val tx = CashpoolTransactions.of(owner = otherUser)
        context(Contexts.of(adminUser)) {
            assertFails {
                CashpoolTransactionPolicy.canUpdate(cmd, isOpened = false, isMember = true, transaction = tx)
            }
        }
    }

    @Test
    fun `canAttachImage - owner`() {
        context(Contexts.of(currentUser)) {
            CashpoolTransactionPolicy.canAttachImage(isOpened = true, isMember = true, transaction = transaction)
        }
    }

    @Test
    fun `canAttachImage - non-owner fails`() {
        context(Contexts.of(otherUser)) {
            assertFails {
                CashpoolTransactionPolicy.canAttachImage(
                    isOpened = true,
                    isMember = true,
                    transaction = transaction
                )
            }
        }
    }

    @Test
    fun `canDelete - admin override`() {
        context(Contexts.of(adminUser)) {
            CashpoolTransactionPolicy.canDelete(isOpened = true, isMember = true, transaction = transaction)
        }
    }

    @Test
    fun `canDelete - owner`() {
        context(Contexts.of(currentUser)) {
            CashpoolTransactionPolicy.canDelete(isOpened = true, isMember = true, transaction = transaction)
        }
    }

    @Test
    fun `canDelete - closed cashpool fails non-admin`() {
        context(Contexts.of(currentUser)) {
            assertFails {
                CashpoolTransactionPolicy.canDelete(
                    isOpened = false,
                    isMember = true,
                    transaction = transaction
                )
            }
        }
    }

    @Test
    fun `canDelete - non-owner fails`() {
        context(Contexts.of(otherUser)) {
            assertFails {
                CashpoolTransactionPolicy.canDelete(
                    isOpened = true,
                    isMember = true,
                    transaction = transaction
                )
            }
        }
    }
}