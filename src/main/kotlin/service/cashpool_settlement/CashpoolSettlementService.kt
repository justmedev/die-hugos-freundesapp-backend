package service.cashpool_settlement

import core.exceptions.CashpoolNotFound
import domain.models.CashpoolSettlement
import service.cashpool.CashpoolService
import service.cashpool_member.CashpoolMemberService
import service.cashpool_transaction.CashpoolTransactionService
import java.math.BigDecimal
import java.math.RoundingMode

class CashpoolSettlementService(
    val cashpoolService: CashpoolService,
    val cashpoolTransactionService: CashpoolTransactionService,
    val cashpoolMemberService: CashpoolMemberService,
) {
    /**
     * Calculate the settlements required to make everybody pay their share of a cashpool.
     */
    suspend fun calculateSettlements(cashpoolId: Int): List<CashpoolSettlement> {
        val cashpool = cashpoolService.findById(cashpoolId)
        val members = cashpoolMemberService.findByCashpoolId(cashpool.id)
        if (members.isEmpty()) return listOf()

        val settlementMembers = mutableListOf<CashpoolSettlementMember>()

        // 1. Calculate the total amount of money that each member moved without Double pollution
        members.forEach { member ->
            val transactionsByMember =
                cashpoolTransactionService.findByCashpoolIdAndTransactionOwnerId(cashpool.id, member.user.id)

            val totalPaid = transactionsByMember.sumOf {
                BigDecimal.valueOf(it.amountCents).movePointLeft(2)
            }

            settlementMembers.add(CashpoolSettlementMember(member, totalPaid))
        }

        // 2. Calculate the fair share, specifying scale and rounding mode
        val total = settlementMembers.sumOf { it.balancePaid }
        // TODO: Support uneven distributions
        val fairShare = total.divide(BigDecimal(members.size), 2, RoundingMode.HALF_UP)

        // Identify debtors and creditors safely using compareTo
        val debtors = settlementMembers
            .filter { (it.balancePaid - fairShare) < BigDecimal.ZERO }
            .toMutableList()
        val creditors = settlementMembers
            .filter { (it.balancePaid - fairShare) > BigDecimal.ZERO }
            .toMutableList()

        // 3. Validate math using a tolerance limit instead of strict equality
        val debtSum = debtors.sumOf { fairShare - it.balancePaid }
        val creditSum = creditors.sumOf { it.balancePaid - fairShare }

        // Allow for a rounding drift of up to 1 cent per member
        val tolerance = BigDecimal("0.01").multiply(BigDecimal(members.size))

        if ((debtSum - creditSum).abs() > tolerance) {
            throw IllegalStateException("Debtors total ($debtSum) and creditors total ($creditSum) mismatch beyond tolerance!")
        }

        val settlements = mutableListOf<CashpoolSettlement>()

        // 4. Resolve debts
        while (debtors.isNotEmpty() && creditors.isNotEmpty()) {
            // Debtors sorted so the biggest debtor is first
            debtors.sortBy { it.balancePaid }
            // Creditors sorted descending so the largest creditor is resolved first
            creditors.sortByDescending { it.balancePaid }

            val highestDebtor = debtors.first()
            val highestCreditor = creditors.first()

            val debtOwed = fairShare - highestDebtor.balancePaid
            val creditDue = highestCreditor.balancePaid - fairShare

            // Determine transaction amount
            val amount = debtOwed.min(creditDue)

            highestDebtor.balancePaid += amount
            highestCreditor.balancePaid -= amount

            settlements.add(
                CashpoolSettlement(
                    from = highestDebtor.member.user,
                    to = highestCreditor.member.user,
                    // Safely scale and shift back to cents
                    amountCents = amount.setScale(2, RoundingMode.HALF_UP).movePointRight(2).toLong()
                )
            )

            // Use compareTo for zero-difference checks
            if (highestDebtor.balancePaid.compareTo(fairShare) == 0) debtors.remove(highestDebtor)
            if (highestCreditor.balancePaid.compareTo(fairShare) == 0) creditors.remove(highestCreditor)
        }

        return settlements
    }
}