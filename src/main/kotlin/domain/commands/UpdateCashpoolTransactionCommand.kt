package domain.commands

import domain.commands.validations.CashpoolTransactionValidations


data class UpdateCashpoolTransactionCommand(
    val ownerId: Int,
    val cashpoolId: Int,
    val transactionId: Int,
    val label: String,
    val amountCents: Long,
) {
    init {
        val validation = CashpoolTransactionValidations.validateUpdateCashpoolTransactionCommand(this)
        if (!validation.isValid) throw IllegalArgumentException(validation.errors.joinToString())
    }
}