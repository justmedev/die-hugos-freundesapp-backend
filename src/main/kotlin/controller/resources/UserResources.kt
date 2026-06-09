package controller.resources

import io.ktor.resources.*
import kotlinx.serialization.Serializable

@Serializable
@Resource("/users")
class UserResource {
    @Serializable
    @Resource("me")
    class Me(val parent: UserResource = UserResource())
}