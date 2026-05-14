package domain.entities

import domain.tables.CashpoolSettlementsTable
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.dao.IntEntity
import org.jetbrains.exposed.v1.dao.IntEntityClass

class CashpoolSettlementEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<CashpoolSettlementEntity>(CashpoolSettlementsTable)

    var from by UserEntity referencedOn CashpoolSettlementsTable.from
    var fromId by CashpoolSettlementsTable.from

    var to by UserEntity referencedOn CashpoolSettlementsTable.to
    var toId by CashpoolSettlementsTable.to

    var cashpool by CashpoolEntity referencedOn CashpoolSettlementsTable.cashpool
    var cashpoolId by CashpoolSettlementsTable.cashpool

    var amountCents by CashpoolSettlementsTable.amountCents
    var purpose by CashpoolSettlementsTable.purpose

    var createdAt by CashpoolSettlementsTable.createdAt
}