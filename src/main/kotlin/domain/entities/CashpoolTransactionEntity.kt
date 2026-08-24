package domain.entities

import domain.tables.CashpoolTransactionsTable
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.dao.IntEntity
import org.jetbrains.exposed.v1.dao.IntEntityClass

class CashpoolTransactionEntity(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<CashpoolTransactionEntity>(CashpoolTransactionsTable)

    var owner by UserEntity referencedOn CashpoolTransactionsTable.owner
    var ownerId by CashpoolTransactionsTable.owner

    var cashpool by CashpoolEntity referencedOn CashpoolTransactionsTable.cashpool
    var cashpoolId by CashpoolTransactionsTable.cashpool

    var amountCents by CashpoolTransactionsTable.amountCents
    var label by CashpoolTransactionsTable.label
    var attachedImageUUID by CashpoolTransactionsTable.attachedImageUUID

    var createdAt by CashpoolTransactionsTable.createdAt
}