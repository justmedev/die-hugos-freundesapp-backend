package domain.entities

import domain.tables.Users
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.dao.IntEntity
import org.jetbrains.exposed.v1.dao.IntEntityClass

class User(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<User>(Users)

    var email by Users.email
    var firstName by Users.firstName
    var lastName by Users.lastName
    var password by Users.password
    var birthdate by Users.birthdate
    var isAdmin by Users.isAdmin
    var createdAt by Users.createdAt
}