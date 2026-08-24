package domain.commands.validations

import core.extensions.optionalUpdate
import domain.commands.CreateCashpoolCommand
import domain.commands.UpdateCashpoolCommand
import io.konform.validation.Validation
import io.konform.validation.ValidationBuilder
import io.konform.validation.constraints.maxLength
import io.konform.validation.constraints.notBlank

object CashpoolValidations {
    private val titleDescriptionValidation: ValidationBuilder<String>.() -> Unit = {
        notBlank()
        maxLength(255)
    }

    val validateCreateCashpoolCommand = Validation {
        CreateCashpoolCommand::title { titleDescriptionValidation() }
        CreateCashpoolCommand::description { titleDescriptionValidation() }
    }

    val validateUpdateCashpoolCommand = Validation {
        UpdateCashpoolCommand::title { optionalUpdate { titleDescriptionValidation() } }
        UpdateCashpoolCommand::description { optionalUpdate { titleDescriptionValidation() } }
    }
}