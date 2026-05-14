package domain.commands.validations

import domain.commands.CreateCashpoolCommand
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
}