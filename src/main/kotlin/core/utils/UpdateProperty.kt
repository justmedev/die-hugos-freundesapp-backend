package core.utils

import core.serialization.UpdatePropertySerializer
import kotlinx.serialization.Serializable

@Serializable(with = UpdatePropertySerializer::class)
data class UpdateProperty<out T>(val value: T?, val update: Boolean) {
    /**
     * Do not update this property.
     */
    constructor() : this(null, false)

    /**
     * Update the property with the given value.
     */
    constructor(value: T) : this(value, true)

    val valueIfUpdated: T?
        get() = if (update) value else null
}