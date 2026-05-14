package domain.commands

import domain.commands.validations.CashpoolValidations


data class UpdateCashpoolCommand(
    val cashpoolId: Int,
    val title: String,
    val description: String,
) {
    init {
        val validation = CashpoolValidations.validateUpdateCashpoolCommand(this)
        if (!validation.isValid) throw IllegalArgumentException(validation.errors.joinToString())
    }
}