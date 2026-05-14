package domain.commands

import domain.commands.validations.CashpoolValidations


data class CreateCashpoolCommand(
    val title: String,
    val description: String,
    val ownerId: Int
) {
    init {
        val validation = CashpoolValidations.validateCreateCashpoolCommand(this)
        if (!validation.isValid) throw IllegalArgumentException(validation.errors.joinToString())
    }
}