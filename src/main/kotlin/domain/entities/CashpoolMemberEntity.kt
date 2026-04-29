package domain.entities

import domain.tables.CashpoolMembersTable
import domain.tables.CashpoolsTable
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.dao.IntEntity
import org.jetbrains.exposed.v1.dao.IntEntityClass

class CashpoolMemberEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<CashpoolMemberEntity>(CashpoolMembersTable)

    var user by UserEntity referencedOn CashpoolMembersTable.user
    var userId by CashpoolMembersTable.user

    var cashpool by CashpoolEntity referencedOn CashpoolMembersTable.cashpool
    var cashpoolId by CashpoolMembersTable.cashpool
    var createdAt by CashpoolsTable.createdAt
}