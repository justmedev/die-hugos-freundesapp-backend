package domain.commands

import core.utils.UpdateProperty
import domain.commands.validations.UserValidations
import domain.models.valueobjects.IBAN
import kotlinx.datetime.LocalDate

data class UpdateUserCommand(
    val email: UpdateProperty<String> = UpdateProperty(),
    val firstName: UpdateProperty<String> = UpdateProperty(),
    val lastName: UpdateProperty<String> = UpdateProperty(),
    val accountHolderName: UpdateProperty<String?> = UpdateProperty(),
    val accountIBAN: UpdateProperty<IBAN?> = UpdateProperty(),
    val birthdate: UpdateProperty<LocalDate> = UpdateProperty(),
) {
    init {
        val validation = UserValidations.validateUpdateUserCommand(this)
        if (!validation.isValid) throw IllegalArgumentException(validation.errors.joinToString())
    }
}