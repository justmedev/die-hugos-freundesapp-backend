package core.utils

import kotlinx.serialization.Serializable

@Serializable
data class UpdateProperty <out T>(val value: T?, val update: Boolean) {
    /**
     * A property that can be updated with [value].
     */
    constructor() : this(null, false)

    val valueIfUpdated: T?
        get() = if (update) value else null
}