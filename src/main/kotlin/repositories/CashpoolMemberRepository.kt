package repositories

import domain.entities.CashpoolMemberEntity
import domain.models.CashpoolMember
import domain.tables.CashpoolMembersTable
import domain.tables.CashpoolsTable
import domain.tables.UsersTable
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction
import service.cashpool_member.CreateCashpoolMemberCommand

interface CashpoolMemberRepository {
    suspend fun create(cmd: CreateCashpoolMemberCommand): CashpoolMember
    suspend fun findById(id: Int): CashpoolMember?
    suspend fun findByCashpoolId(cashpoolId: Int): List<CashpoolMember>
    suspend fun findAll(): List<CashpoolMember>
}

class CashpoolMemberRepositoryImpl : CashpoolMemberRepository {
    override suspend fun create(cmd: CreateCashpoolMemberCommand): CashpoolMember = suspendTransaction {
        CashpoolMember.from(CashpoolMemberEntity.new {
            userId = EntityID(cmd.userId, UsersTable)
            cashpoolId = EntityID(cmd.cashpoolId, CashpoolsTable)
        })!!
    }

    override suspend fun findById(id: Int): CashpoolMember? = suspendTransaction {
        CashpoolMemberEntity.findById(id)?.let { CashpoolMember.from(it) }
    }

    override suspend fun findByCashpoolId(cashpoolId: Int): List<CashpoolMember> = suspendTransaction {
        CashpoolMemberEntity.find { CashpoolMembersTable.cashpool eq cashpoolId }.map { CashpoolMember.from(it)!! }
    }

    override suspend fun findAll(): List<CashpoolMember> = suspendTransaction {
        CashpoolMemberEntity.all().map { CashpoolMember.from(it)!! }.toList()
    }
}