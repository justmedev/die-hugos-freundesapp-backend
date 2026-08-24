package dto.cashpool

import core.utils.UpdateProperty
import kotlinx.serialization.Serializable

@Serializable
data class UpdateCashpoolRequest(
    val title: UpdateProperty<String> = UpdateProperty(),
    val description: UpdateProperty<String> = UpdateProperty(),
    val isOpened: UpdateProperty<Boolean> = UpdateProperty(),
)