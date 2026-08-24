package domain.commands.validations

import core.extensions.optionalUpdate
import domain.commands.CreateCashpoolTransactionCommand
import domain.commands.UpdateCashpoolTransactionCommand
import io.konform.validation.Validation
import io.konform.validation.ValidationBuilder
import io.konform.validation.constraints.maxLength
import io.konform.validation.constraints.minimum
import io.konform.validation.constraints.notBlank
import io.konform.validation.onEach

object CashpoolTransactionValidations {
    private val labelValidation: ValidationBuilder<String>.() -> Unit = {
        notBlank()
        maxLength(255)
    }
    private val excludedUsersValidation: ValidationBuilder<Int>.() -> Unit = {
        minimum(1)
    }

    val validateCreateCashpoolTransactionCommand = Validation {
        CreateCashpoolTransactionCommand::label { labelValidation() }
        CreateCashpoolTransactionCommand::excludedUsers onEach { excludedUsersValidation() }
    }

    val validateUpdateCashpoolTransactionCommand = Validation {
        UpdateCashpoolTransactionCommand::label { optionalUpdate { labelValidation() } }
        UpdateCashpoolTransactionCommand::excludedUsers { optionalUpdate { onEach { excludedUsersValidation() } } }
    }
}