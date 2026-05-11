package service.cashpool_settlement

import domain.models.CashpoolMember
import java.math.BigDecimal

data class CashpoolSettlementMember(
    val member: CashpoolMember,
    var balancePaid: BigDecimal,
) {
    override fun toString(): String {
        return "CashpoolSettlementMember(\"${member.user.firstName} ${member.user.lastName}\", $balancePaid €)"
    }
}
