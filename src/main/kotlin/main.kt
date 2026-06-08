import domain.tables.*
import io.ktor.server.application.*
import io.ktor.server.netty.*
import org.jetbrains.exposed.v1.core.Table
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction


fun main(args: Array<String>) {
    EngineMain.main(args)
}

val tables: Array<Table>
    get() = arrayOf(
        UsersTable,
        CashpoolsTable,
        CashpoolMembersTable,
        CashpoolTransactionsTable,
        CashpoolSettlementsTable
    )

suspend fun Application.main() {
    suspendTransaction {
        SchemaUtils.create(*tables)
    }
}