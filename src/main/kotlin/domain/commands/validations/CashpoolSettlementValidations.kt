package domain.commands.validations

import domain.commands.CreateCashpoolSettlementCommand
import domain.commands.CreateCashpoolTransactionCommand
import domain.commands.UpdateCashpoolTransactionCommand
import io.konform.validation.Validation
import io.konform.validation.ValidationBuilder
import io.konform.validation.constraints.maxLength
import io.konform.validation.constraints.notBlank

object CashpoolSettlementValidations {
    private val purposeValidation: ValidationBuilder<String>.() -> Unit = {
        maxLength(140)
    }

    val validateCreateCashpoolSettlementCommand = Validation {
        CreateCashpoolSettlementCommand::purpose { purposeValidation() }
    }
}