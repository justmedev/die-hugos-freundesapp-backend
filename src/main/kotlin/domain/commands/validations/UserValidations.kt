package domain.commands.validations

import core.extensions.optionalUpdate
import domain.commands.CreateUserCommand
import domain.commands.UpdateUserCommand
import domain.models.valueobjects.IBAN
import io.konform.validation.Validation
import io.konform.validation.ValidationBuilder
import io.konform.validation.constraints.maxLength
import io.konform.validation.constraints.maximum
import io.konform.validation.constraints.notBlank
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import kotlin.time.Clock

object UserValidations {
    private val nameValidation: ValidationBuilder<String>.() -> Unit = {
        notBlank()
        maxLength(128)
    }

    private val accountHolderNameValidation: ValidationBuilder<String>.() -> Unit = {
        notBlank()
        maxLength(255)
    }

    private val accountIBANValidation: ValidationBuilder<IBAN>.() -> Unit = {
        validate("trimmedIBAN", { it.value.trim() }) {
            maxLength(34)
        }
    }

    private val birthdateValidation: ValidationBuilder<LocalDate>.() -> Unit = {
        validate("birthdate", { it.compareTo(Clock.System.todayIn(TimeZone.UTC)) }) {
            maximum(0)
        }
    }

    val validateCreateUserCommand = Validation {
        CreateUserCommand::firstName { nameValidation() }
        CreateUserCommand::lastName { nameValidation() }
        CreateUserCommand::accountHolderName ifPresent { accountHolderNameValidation() }
        CreateUserCommand::accountIBAN ifPresent { accountIBANValidation() }
        CreateUserCommand::birthdate { birthdateValidation() }
    }

    val validateUpdateUserCommand = Validation {
        UpdateUserCommand::firstName { optionalUpdate { nameValidation() } }
        UpdateUserCommand::lastName { optionalUpdate { nameValidation() } }
        UpdateUserCommand::accountHolderName { optionalUpdate { accountHolderNameValidation() } }
        UpdateUserCommand::accountIBAN { optionalUpdate { accountIBANValidation() } }
        UpdateUserCommand::birthdate { optionalUpdate { birthdateValidation() } }
    }
}