import domain.tables.CashpoolMembersTable
import domain.tables.CashpoolTransactionsTable
import domain.tables.CashpoolsTable
import domain.tables.UsersTable
import io.ktor.server.application.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.di.*
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction
import service.auth.AuthService
import domain.commands.RegisterCommand
import service.user.UserService
import kotlin.time.Clock


fun main(args: Array<String>) {
    EngineMain.main(args)
}

val tables: Array<Table>
    get() = arrayOf(UsersTable, CashpoolsTable, CashpoolMembersTable, CashpoolTransactionsTable)

suspend fun Application.main() {

    suspendTransaction {
        SchemaUtils.create(*tables)
    }

    // Create admin user if not exists
    val userService: UserService by dependencies
    val authService: AuthService by dependencies

    val adminEmail = environment.config.property("diehugos.adminuser.email").getString()
    val adminPassword = environment.config.property("diehugos.adminuser.password").getString()

    userService.findByEmail(adminEmail) ?: authService.register(
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