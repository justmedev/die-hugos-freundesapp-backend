package domain.tables

import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.dao.id.IntIdTable
import org.jetbrains.exposed.v1.core.java.javaUUID
import org.jetbrains.exposed.v1.datetime.CurrentDateTime
import org.jetbrains.exposed.v1.datetime.datetime

object CashpoolTransactionsTable : IntIdTable("cashpool_transactions") {
    var owner = reference("owner_id", UsersTable)
    var cashpool = reference("cashpool_id", CashpoolsTable, onDelete = ReferenceOption.CASCADE)
    var amountCents = long("amount_cents")
    var label = varchar("label", 255)
    var attachedImageUUID = javaUUID("attached_image_uuid").nullable()
    var excludedUsers = array<Int>("excluded_users").default(emptyList())
    val createdAt = datetime("created_at").defaultExpression(CurrentDateTime)
}
