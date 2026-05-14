package domain.commands.validations

import io.konform.validation.ValidationBuilder
import io.konform.validation.constraints.containsPattern
import io.konform.validation.constraints.maxLength
import io.konform.validation.constraints.notBlank

object AuthValidations {
    val emailValidation: ValidationBuilder<String>.() -> Unit = {
        notBlank()
        containsPattern("@")
        maxLength(254)
    }

    val passwordValidation: ValidationBuilder<String>.() -> Unit = {
        notBlank()
        maxLength(128)
    }
}