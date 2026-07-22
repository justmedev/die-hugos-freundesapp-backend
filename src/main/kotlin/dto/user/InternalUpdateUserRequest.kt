package dto.user

import core.utils.UpdateProperty
import domain.models.valueobjects.IBAN
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

@Serializable
data class InternalUpdateUserRequest(
    val email: UpdateProperty<String> = UpdateProperty(),
    val firstName: UpdateProperty<String> = UpdateProperty(),
    val lastName: UpdateProperty<String> = UpdateProperty(),
    val accountHolderName: UpdateProperty<String?> = UpdateProperty(),
    val accountIBAN: UpdateProperty<IBAN?> = UpdateProperty(),
    val birthdate: UpdateProperty<LocalDate> = UpdateProperty(),
)