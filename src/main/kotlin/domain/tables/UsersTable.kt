package domain.tables

import org.jetbrains.exposed.v1.core.dao.id.IntIdTable
import org.jetbrains.exposed.v1.datetime.CurrentDateTime
import org.jetbrains.exposed.v1.datetime.date
import org.jetbrains.exposed.v1.datetime.datetime

object UsersTable : IntIdTable("users") {
    val email = varchar("email", 254)
    val firstName = varchar("first_name", 128)
    val lastName = varchar("last_name", 128)
    val accountHolderName = varchar("account_holder_name", 255).nullable()
    val accountIBAN = varchar("account_iban", 50).nullable()
    val password = varchar("password", 128)
    val birthdate = date("birthdate")
    val isAdmin = bool("is_admin").default(false)
    val createdAt = datetime("created_at").defaultExpression(CurrentDateTime)
}
