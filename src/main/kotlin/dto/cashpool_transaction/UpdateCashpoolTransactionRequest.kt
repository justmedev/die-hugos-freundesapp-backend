package dto.cashpool_transaction

import core.utils.UpdateProperty
import kotlinx.serialization.Serializable

@Serializable
data class UpdateCashpoolTransactionRequest(
    val label: UpdateProperty<String> = UpdateProperty(),
    val amountCents: UpdateProperty<Long> = UpdateProperty(),
    val excludedUsers: UpdateProperty<List<Int>> = UpdateProperty(),
)