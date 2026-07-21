package service.cashpool_settlement

import core.exceptions.Forbidden
import domain.models.CashpoolSuggestedSettlement
import domain.models.CashpoolUserSettlementSummary
import service.cashpool.CashpoolService
import service.cashpool_member.CashpoolMemberService
import service.cashpool_transaction.CashpoolTransactionService
import service.user.UserService
import java.math.BigDecimal
import java.math.RoundingMode

class CashpoolSuggestedSettlementCalculationService(
    val cashpoolService: CashpoolService,
    val cashpoolSettlementService: CashpoolSettlementService,
    val cashpoolTransactionService: CashpoolTransactionService,
    val cashpoolMemberService: CashpoolMemberService,
    val userService: UserService,
) {
    /**
     * Calculate the settlements required to make everybody pay their share of a cashpool.
     */
    suspend fun calculateSettlements(cashpoolId: Int, requestingUserId: Int): List<CashpoolSuggestedSettlement> {
        val cashpool = cashpoolService.findByIdOnlyIfMember(cashpoolId, requestingUserId)
        val members = cashpoolMemberService.findByCashpoolId(cashpool.id)
        if (members.isEmpty()) return listOf()

        // 1. Fetch all transactions and group them by owner to avoid N+1 database calls
        val allTransactions = cashpoolTransactionService.findByCashpoolId(cashpool.id, requestingUserId)
        val transactionsByOwner = allTransactions.groupBy { it.owner.id }

        val settlements = cashpoolSettlementService.findByCashpoolId(cashpool.id, requestingUserId)
        val settlementMembers = members.map { member ->
            // positive -> owed, negative -> credited
            var totalPaid = transactionsByOwner[member.user.id]?.sumOf {
                // Negate so expenses become positive contributions to the pool
                BigDecimal.valueOf(it.amountCents).movePointLeft(2).negate()
            } ?: BigDecimal.ZERO

            settlements.filter { it.from.id == member.user.id }.forEach {
                totalPaid += BigDecimal.valueOf(it.amountCents).movePointLeft(2)
            }
            settlements.filter { it.to.id == member.user.id }.forEach {
                totalPaid -= BigDecimal.valueOf(it.amountCents).movePointLeft(2)
            }

            CashpoolSuggestedSettlementCalculationMember(member, totalPaid)
        }

        // 2. Calculate the fair share, specifying scale and rounding mode
        val total = settlementMembers.sumOf { it.balancePaid }
        // TODO: Support uneven distributions
        val fairShare = total.divide(BigDecimal(members.size), 2, RoundingMode.HALF_UP)
        println("Total: $total Fair share: $fairShare")

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

        val suggestedSettlements = mutableListOf<CashpoolSuggestedSettlement>()

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

            suggestedSettlements.add(
                CashpoolSuggestedSettlement(
                    from = highestDebtor.member.user,
                    to = highestCreditor.member.user,
                    // Safely scale and shift back to cents
                    amountCents = amount.setScale(2, RoundingMode.HALF_UP).movePointRight(2).toLong()
                )
            )

            if ((highestDebtor.balancePaid - fairShare).abs() <= tolerance) debtors.remove(highestDebtor)
            if ((highestCreditor.balancePaid - fairShare).abs() <= tolerance) creditors.remove(highestCreditor)
        }

        return suggestedSettlements
    }

    /**
     * Calculate the summary which includes netUserBalance (how much a given user owes or is owed in total) and totalOpenCashpoolBalance (the amount of money a cashpool is worth - all executed settlements)
     */
    suspend fun calculateUserSettlementSummary(
        cashpoolId: Int,
        userId: Int,
        requestingUserId: Int
    ): CashpoolUserSettlementSummary {
        val requestingUser = userService.findById(requestingUserId)

        if (userId != requestingUser.id && !requestingUser.isAdmin) throw Forbidden("You are only allowed to access your own summary!");

        val allSettlements = calculateSettlements(cashpoolId, requestingUserId)
        val netUserBalance = allSettlements.sumOf { settlement ->
            var addend = 0L
            if (settlement.from.id == userId) addend -= settlement.amountCents // We owe, so negative
            if (settlement.to.id == userId) addend += settlement.amountCents // We receive, so positive
            addend
        }
        val allTransactions = cashpoolTransactionService.findByCashpoolId(cashpoolId, requestingUserId)
        val totalOpenCashpoolBalance =
            allTransactions.sumOf { it.amountCents } - allSettlements.sumOf { it.amountCents }

        return CashpoolUserSettlementSummary(
            netUserBalance,
            totalOpenCashpoolBalance,
        )
    }
}