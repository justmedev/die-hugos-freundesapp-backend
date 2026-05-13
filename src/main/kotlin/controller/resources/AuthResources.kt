package controller.resources

import io.ktor.resources.*

@Resource("/auth")
class AuthResource {

    @Resource("login")
    class Login(val parent: AuthResource = AuthResource())

    @Resource("refresh")
    class Refresh(val parent: AuthResource = AuthResource())
}