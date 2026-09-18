package service.cashpool_member

import core.exceptions.*
import domain.commands.CreateCashpoolMemberCommand
import domain.contexts.ServiceContext
import domain.models.CashpoolMember
import domain.policies.CashpoolMemberPolicy
import domain.policies.CashpoolPolicy
import domain.repositories.CashpoolMemberRepository
import domain.repositories.CashpoolRepository
import domain.repositories.UserRepository
import org.jetbrains.exposed.v1.exceptions.ExposedSQLException

class CashpoolMemberService(
    private val cashpoolMemberRepo: CashpoolMemberRepository,
    private val userRepo: UserRepository,
    private val cashpoolRepo: CashpoolRepository,
) {
    context(ctx: ServiceContext)
    suspend fun create(cmd: CreateCashpoolMemberCommand): CashpoolMember {
        userRepo.findById(cmd.userId) ?: throw UserNotFound()
        cashpoolRepo.findById(cmd.cashpoolId) ?: throw CashpoolNotFound()
        CashpoolMemberPolicy.canCreate(cmd)

        return try {
            cashpoolMemberRepo.create(cmd)
        } catch (e: ExposedSQLException) {
            if (e.message?.contains("Unique index or primary key violation") == true) {
                throw Conflict("User is already a member of this cashpool.")
            }
            throw e;
        }
    }

    context(ctx: ServiceContext)
    suspend fun findById(id: Int): CashpoolMember {
        val cashpoolMember = cashpoolMemberRepo.findById(id) ?: throw CashpoolMemberNotFound()
        CashpoolMemberPolicy.canView(cashpoolMember)
        return cashpoolMember
    }

    context(ctx: ServiceContext)
    suspend fun findByCashpoolId(cashpoolId: Int): List<CashpoolMember> {
        val isMember = cashpoolRepo.isMember(cashpoolId, ctx.user.id)
        CashpoolPolicy.canView(isMember)
        return cashpoolMemberRepo.findByCashpoolId(cashpoolId)
    }

    context(ctx: ServiceContext)
    suspend fun findAll(): List<CashpoolMember> {
        CashpoolMemberPolicy.canView(null)
        return cashpoolMemberRepo.findAllByUserId(ctx.user.id)
    }
}