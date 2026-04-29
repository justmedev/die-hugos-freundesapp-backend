package domain.tables

import org.jetbrains.exposed.v1.core.dao.id.IntIdTable
import org.jetbrains.exposed.v1.datetime.CurrentDateTime
import org.jetbrains.exposed.v1.datetime.datetime

object CashpoolMembersTable : IntIdTable("cashpool_members") {
    var user = reference("user_id", UsersTable)
    var cashpool = reference("cashpool_id", CashpoolsTable)
    val createdAt = datetime("created_at").defaultExpression(CurrentDateTime)
}
