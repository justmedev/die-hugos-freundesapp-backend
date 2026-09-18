package domain.policies

import org.junit.Test
import testutils.Contexts
import testutils.Users
import kotlin.test.assertFails

class UserPolicyTest {
    private val currentUser = Users.nonAdminUser
    private val otherUser = Users.nonAdminUser.copy(id = currentUser.id + 1)
    private val adminUser = Users.nonAdminUser.copy(isAdmin = true)

    @Test
    fun `canCreate - admin override`() {
        context(Contexts.of(adminUser)) {
            UserPolicy.canCreate()
        }
    }

    @Test
    fun `canCreate - non-admin fails`() {
        context(Contexts.of(currentUser)) {
            assertFails { UserPolicy.canCreate() }
        }
    }

    @Test
    fun `canView - self`() {
        context(Contexts.of(currentUser)) {
            UserPolicy.canView(currentUser.id)
        }
    }

    @Test
    fun `canView - other user fails`() {
        context(Contexts.of(currentUser)) {
            assertFails { UserPolicy.canView(otherUser.id) }
        }
    }

    @Test
    fun `canView - should fail when id is null`() {
        context(Contexts.of(currentUser)) {
            assertFails { UserPolicy.canView(id = null) }
        }
    }

    @Test
    fun `canUpdate - self`() {
        context(Contexts.of(currentUser)) {
            UserPolicy.canUpdate(currentUser.id)
        }
    }

    @Test
    fun `canUpdate - other user fails`() {
        context(Contexts.of(currentUser)) {
            assertFails { UserPolicy.canUpdate(otherUser.id) }
        }
    }
}