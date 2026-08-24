package domain.commands

import domain.commands.validations.CashpoolTransactionValidations


data class CreateCashpoolTransactionCommand(
    val ownerId: Int,
    val cashpoolId: Int,
    val label: String,
    val amountCents: Long,
    val excludedUsers: List<Int>,
) {
    init {
        val validation = CashpoolTransactionValidations.validateCreateCashpoolTransactionCommand(this)
        if (!validation.isValid) throw IllegalArgumentException(validation.errors.joinToString())
    }
}