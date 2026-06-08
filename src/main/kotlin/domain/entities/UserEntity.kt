package domain.entities

import domain.tables.UsersTable
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.dao.IntEntity
import org.jetbrains.exposed.v1.dao.IntEntityClass

class UserEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<UserEntity>(UsersTable)

    var authentikId by UsersTable.authentikId
    var email by UsersTable.email
    var firstName by UsersTable.firstName
    var lastName by UsersTable.lastName
    var accountHolderName by UsersTable.accountHolderName
    var accountIBAN by UsersTable.accountIBAN
    var birthdate by UsersTable.birthdate
    var isAdmin by UsersTable.isAdmin
    var createdAt by UsersTable.createdAt
}