package dto.user

import core.utils.UpdateProperty
import domain.models.valueobjects.IBAN
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

@Serializable
data class UpdateUserRequest(
    val email: UpdateProperty<String>,
    val firstName: UpdateProperty<String>,
    val lastName: UpdateProperty<String>,
    val accountHolderName: UpdateProperty<String?> = UpdateProperty(),
    val accountIBAN: UpdateProperty<IBAN?> = UpdateProperty(),
    val birthdate: UpdateProperty<LocalDate>,
)