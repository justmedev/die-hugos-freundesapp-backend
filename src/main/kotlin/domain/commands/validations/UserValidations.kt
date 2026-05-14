package domain.commands.validations

import domain.commands.CreateUserCommand
import domain.commands.RegisterCommand
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
        CreateUserCommand::email { AuthValidations.emailValidation }
        CreateUserCommand::firstName { nameValidation() }
        CreateUserCommand::lastName { nameValidation() }
        CreateUserCommand::accountHolderName ifPresent { accountHolderNameValidation() }
        CreateUserCommand::accountIBAN ifPresent { accountIBANValidation() }
        CreateUserCommand::passwordHash {
            notBlank()
            maxLength(128)
        }
        CreateUserCommand::birthdate { birthdateValidation() }
    }

    val validateUpdateUserCommand = Validation {
        UpdateUserCommand::email { AuthValidations.emailValidation }
        UpdateUserCommand::firstName { nameValidation() }
        UpdateUserCommand::lastName { nameValidation() }
        UpdateUserCommand::accountHolderName ifPresent { accountHolderNameValidation() }
        UpdateUserCommand::accountIBAN ifPresent { accountIBANValidation() }
        UpdateUserCommand::birthdate { birthdateValidation() }
    }

    val validateRegisterCommand = Validation {
        RegisterCommand::email { AuthValidations.emailValidation }
        RegisterCommand::firstName { nameValidation() }
        RegisterCommand::lastName { nameValidation() }
        RegisterCommand::plaintextPassword { AuthValidations.passwordValidation }
        RegisterCommand::accountHolderName ifPresent { accountHolderNameValidation() }
        RegisterCommand::accountIBAN ifPresent { accountIBANValidation() }
        RegisterCommand::birthdate { birthdateValidation() }
    }
}