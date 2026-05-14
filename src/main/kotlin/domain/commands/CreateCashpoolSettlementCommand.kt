package domain.commands

import domain.commands.validations.CashpoolSettlementValidations


data class CreateCashpoolSettlementCommand(
    val fromId: Int,
    val toId: Int,
    val cashpoolId: Int,
    val purpose: String,
    val amountCents: Long,
) {
    init {
        val validation = CashpoolSettlementValidations.validateCreateCashpoolSettlementCommand(this)
        if (!validation.isValid) throw IllegalArgumentException(validation.errors.joinToString())
    }
}