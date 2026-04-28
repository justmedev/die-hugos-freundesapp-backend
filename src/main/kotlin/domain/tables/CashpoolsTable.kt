package domain.tables

import org.jetbrains.exposed.v1.core.dao.id.IntIdTable
import org.jetbrains.exposed.v1.datetime.CurrentDateTime
import org.jetbrains.exposed.v1.datetime.datetime

object CashpoolsTable : IntIdTable("cashpools") {
    val title = varchar("title", 255)
    val description = varchar("description", 255)
    val isOpened = bool("is_opened").default(true)
    var owner = reference("owner_id", UsersTable)
    val createdAt = datetime("created_at").defaultExpression(CurrentDateTime)
}
