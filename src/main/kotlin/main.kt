import domain.tables.CashpoolMembersTable
import domain.tables.CashpoolsTable
import domain.tables.UsersTable
import io.ktor.server.application.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.di.*
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction
import service.auth.AuthService
import service.auth.RegisterCommand
import service.users.UsersService
import kotlin.time.Clock


fun main(args: Array<String>) {
    EngineMain.main(args)
}

suspend fun Application.main() {
    suspendTransaction {
        SchemaUtils.create(UsersTable, CashpoolsTable, CashpoolMembersTable)
    }

    // Create admin user if not exists
    val usersService: UsersService by dependencies
    val authService: AuthService by dependencies

    val adminEmail = environment.config.property("diehugos.adminuser.email").getString()
    val adminPassword = environment.config.property("diehugos.adminuser.password").getString()

    usersService.findByEmail(adminEmail) ?: authService.register(
        RegisterCommand(
            email = adminEmail,
            plaintextPassword = adminPassword,
            firstName = "Diego",
            lastName = "Hugo",
            birthdate = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()),
            isAdmin = true
        )
    )
}