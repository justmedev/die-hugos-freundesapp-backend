package service.cashpool_settlement

import domain.models.Cashpool
import domain.models.CashpoolSettlement
import io.ktor.server.plugins.*
import service.cashpool.CashpoolService
import service.cashpool_member.CashpoolMemberService
import service.cashpool_transaction.CashpoolTransactionService

class CashpoolSettlementService(
    val cashpoolService: CashpoolService,
    val cashpoolTransactionService: CashpoolTransactionService,
    val cashpoolMemberService: CashpoolMemberService,
) {
    /**
     * Calculate the settlements required to make everybody pay their share of a cashpool.
     */
    suspend fun calculateSettlements(cashpoolId: Int): List<CashpoolSettlement> {
        val cashpool = cashpoolService.findById(cashpoolId) ?: throw NotFoundException("Cashpool not found")
        val members = cashpoolMemberService.findByCashpoolId(cashpool.id)

        val settlementMembers = mutableListOf<CashpoolSettlementMember>()

        // 1. Calculate the total amount of money that each member moved.
        members.forEach { member ->
            val transactionsByMember =
                cashpoolTransactionService.findByCashpoolIdAndOwnerId(cashpool.id, member.user.id)

            settlementMembers.add(CashpoolSettlementMember(member, transactionsByMember.sumOf { it.amountCents }))
        }

        // 2. Calculate the total amount of money that each member should pay
        val totalAmountCents = settlementMembers.sumOf { it.totalAmountCentsMoved }
        // TODO: support uneven distributions, e.g. one person being excluded by one transaction
        val totalAmountCentsPerMember = totalAmountCents / members.size

        // 3. Calculate the settlements required to reach totalAmountCentsPerMember for each member
        print(totalAmountCentsPerMember)
        return listOf()
    }
}