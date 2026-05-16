package domain.commands.validations

import domain.commands.LoginCommand
import io.konform.validation.Validation

object LoginValidations {
    val validateLoginCommand = Validation {
        LoginCommand::email { AuthValidations.emailValidation(this) }
        LoginCommand::password { AuthValidations.passwordValidation(this) }
    }
}