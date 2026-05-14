package domain.commands

import domain.commands.validations.LoginValidations

data class LoginCommand(
    val email: String,
    val password: String,
) {
    init {
        val validation = LoginValidations.validateLoginCommand(this)
        if (!validation.isValid) throw IllegalArgumentException(validation.errors.joinToString())
    }
}