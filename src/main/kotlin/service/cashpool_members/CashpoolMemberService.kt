package service.cashpool_members

import domain.entities.CashpoolMemberEntity
import domain.models.CashpoolMember
import domain.tables.CashpoolMembersTable
import domain.tables.CashpoolsTable
import domain.tables.UsersTable
import io.ktor.server.plugins.*
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction
import service.cashpools.CashpoolsService
import service.users.UsersService

class CashpoolMemberService(
    private val usersService: UsersService,
    private val cashpoolsService: CashpoolsService,
) {

    suspend fun create(cmd: CreateCashpoolMemberCommand): CashpoolMember {
        usersService.findById(cmd.userId) ?: throw NotFoundException("User not found")
        cashpoolsService.findById(cmd.cashpoolId) ?: throw NotFoundException("Cashpool not found")

        return suspendTransaction {
            return@suspendTransaction CashpoolMember.from(CashpoolMemberEntity.new {
                userId = EntityID(cmd.userId, UsersTable)
                cashpoolId = EntityID(cmd.cashpoolId, CashpoolsTable)
            })!!
        }
    }

    suspend fun findByCashpoolIdAndUserId(cashpoolId: Int, userId: Int) = suspendTransaction {
        CashpoolMember.from(
            CashpoolMemberEntity
            .find { (CashpoolMembersTable.cashpool eq cashpoolId) and (CashpoolMembersTable.user eq userId) }
            .firstOrNull())
    }

    suspend fun findById(id: Int) = suspendTransaction {
        CashpoolMember.from(CashpoolMemberEntity.find { CashpoolMembersTable.id eq id }.firstOrNull())
    }

    suspend fun findAll() = suspendTransaction { CashpoolMemberEntity.all().map { CashpoolMember.from(it) } }
}