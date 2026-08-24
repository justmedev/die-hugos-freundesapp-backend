package domain.commands

import core.utils.UpdateProperty
import domain.commands.validations.CashpoolTransactionValidations

data class UpdateCashpoolTransactionCommand(
    val ownerId: Int,
    val cashpoolId: Int,
    val transactionId: Int,
    val label: UpdateProperty<String> = UpdateProperty(),
    val amountCents: UpdateProperty<Long> = UpdateProperty(),
) {
    init {
        val validation = CashpoolTransactionValidations.validateUpdateCashpoolTransactionCommand(this)
        if (!validation.isValid) throw IllegalArgumentException(validation.errors.joinToString())
    }
}