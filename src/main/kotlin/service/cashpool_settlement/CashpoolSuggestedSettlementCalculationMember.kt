package service.cashpool_settlement

import domain.models.CashpoolMember
import java.math.BigDecimal

data class CashpoolSuggestedSettlementCalculationMember(
    val member: CashpoolMember,
    var balancePaid: BigDecimal,
) {
    override fun toString(): String {
        return "CashpoolSuggestedSettlementCalculationMember(\"${member.user.firstName} ${member.user.lastName}\", $balancePaid €)"
    }
}
