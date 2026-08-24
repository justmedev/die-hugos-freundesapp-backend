package domain.commands

import core.utils.UpdateProperty
import domain.commands.validations.CashpoolValidations

data class UpdateCashpoolCommand(
    val cashpoolId: Int,
    val title: UpdateProperty<String> = UpdateProperty(),
    val description: UpdateProperty<String> = UpdateProperty(),
    val isOpened: UpdateProperty<Boolean> = UpdateProperty(),
) {
    init {
        val validation = CashpoolValidations.validateUpdateCashpoolCommand(this)
        if (!validation.isValid) throw IllegalArgumentException(validation.errors.joinToString())
    }
}