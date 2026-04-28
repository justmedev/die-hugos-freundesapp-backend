package domain.entities

import domain.tables.CashpoolsTable
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.dao.IntEntity
import org.jetbrains.exposed.v1.dao.IntEntityClass

class CashpoolEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<CashpoolEntity>(CashpoolsTable)

    var title by CashpoolsTable.title
    var description by CashpoolsTable.description
    var isOpened by CashpoolsTable.isOpened
    var owner by UserEntity referencedOn CashpoolsTable.owner
    var ownerId by CashpoolsTable.owner
    var createdAt by CashpoolsTable.createdAt
}