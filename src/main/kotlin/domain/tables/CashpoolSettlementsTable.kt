package domain.tables

import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.dao.id.IntIdTable
import org.jetbrains.exposed.v1.datetime.CurrentDateTime
import org.jetbrains.exposed.v1.datetime.datetime

object CashpoolSettlementsTable : IntIdTable("cashpool_settlements") {
    var from = reference("from_id", UsersTable)
    var to = reference("to_id", UsersTable)
    var cashpool = reference("cashpool_id", CashpoolsTable, onDelete = ReferenceOption.CASCADE)
    var amountCents = long("amount_cents")
    var purpose = varchar("purpose", 140)
    val createdAt = datetime("created_at").defaultExpression(CurrentDateTime)
}
