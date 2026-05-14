package domain.commands.validations

import domain.commands.CreateCashpoolTransactionCommand
import domain.commands.UpdateCashpoolTransactionCommand
import io.konform.validation.Validation
import io.konform.validation.ValidationBuilder
import io.konform.validation.constraints.maxLength
import io.konform.validation.constraints.notBlank

object CashpoolTransactionValidations {
    private val labelValidation: ValidationBuilder<String>.() -> Unit = {
        notBlank()
        maxLength(255)
    }

    val validateCreateCashpoolTransactionCommand = Validation {
        CreateCashpoolTransactionCommand::label { labelValidation() }
    }

    val validateUpdateCashpoolTransactionCommand = Validation {
        UpdateCashpoolTransactionCommand::label { labelValidation() }
    }
}