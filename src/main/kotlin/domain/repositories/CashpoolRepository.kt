package domain.repositories

import domain.commands.CreateCashpoolCommand
import domain.commands.UpdateCashpoolCommand
import domain.entities.CashpoolEntity
import domain.models.Cashpool
import domain.tables.CashpoolMembersTable
import domain.tables.CashpoolsTable
import domain.tables.UsersTable
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.select
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction

interface CashpoolRepository {
    suspend fun create(cmd: CreateCashpoolCommand): Cashpool
    suspend fun findById(id: Int): Cashpool?
    suspend fun findAll(): List<Cashpool>
    suspend fun isMember(cashpoolId: Int, userId: Int): Boolean
    suspend fun update(cmd: UpdateCashpoolCommand): Cashpool?
    suspend fun deleteById(id: Int)
}

class CashpoolRepositoryImpl : CashpoolRepository {
    override suspend fun create(cmd: CreateCashpoolCommand): Cashpool = suspendTransaction {
        Cashpool.from(CashpoolEntity.new {
            title = cmd.title
            description = cmd.description
            ownerId = EntityID(cmd.ownerId, UsersTable)
        })!!
    }

    override suspend fun findById(id: Int): Cashpool? = suspendTransaction {
        CashpoolEntity.findById(id)?.let { Cashpool.from(it) }
    }

    override suspend fun findAll(): List<Cashpool> = suspendTransaction {
        CashpoolEntity.all().map { Cashpool.from(it)!! }.toList()
    }

    override suspend fun update(cmd: UpdateCashpoolCommand): Cashpool? = suspendTransaction {
        Cashpool.from(CashpoolEntity.findByIdAndUpdate(cmd.cashpoolId) {
            it.apply {
                title = cmd.title
                description = cmd.description
            }
        })
    }

    override suspend fun deleteById(id: Int): Unit = suspendTransaction {
        CashpoolEntity.findById(id)?.delete()
    }

    override suspend fun isMember(cashpoolId: Int, userId: Int): Boolean = suspendTransaction {
        CashpoolMembersTable.select(CashpoolMembersTable.id)
            .where { (CashpoolMembersTable.cashpool eq cashpoolId) and (CashpoolMembersTable.user eq userId) }
            .limit(1)
            .any()
    }
}