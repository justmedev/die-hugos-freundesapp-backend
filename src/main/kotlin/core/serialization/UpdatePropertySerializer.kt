package core.serialization

import core.utils.UpdateProperty
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

class UpdatePropertySerializer<T>(
    private val tSerializer: KSerializer<T>
) : KSerializer<UpdateProperty<T>> {

    override val descriptor: SerialDescriptor = tSerializer.descriptor

    @OptIn(ExperimentalSerializationApi::class)
    override fun deserialize(decoder: Decoder): UpdateProperty<T> {
        // If this method is called, the key WAS present in the JSON payload.
        return if (decoder.decodeNotNullMark()) {
            val value = decoder.decodeSerializableValue(tSerializer)
            UpdateProperty(value, true)
        } else {
            decoder.decodeNull()
            @Suppress("UNCHECKED_CAST")
            UpdateProperty(null as T, true) // Explicit null passed in JSON
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    override fun serialize(encoder: Encoder, value: UpdateProperty<T>) {
        // Defines how the property looks if you serialize the command back to JSON
        if (value.value != null) {
            encoder.encodeSerializableValue(tSerializer, value.value)
        } else {
            encoder.encodeNull()
        }
    }
}
