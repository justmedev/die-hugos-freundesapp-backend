package core.serialization

import core.exceptions.DataQualityException
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

/**
 * A generic serializer for Value Objects.
 *
 * @param T The Value Object type (e.g., Iban)
 * @param V The underlying primitive/wrapped type (e.g., String)
 * @param underlyingSerializer The KSerializer of the underlying type
 * @param construct Function to create the Value Object from the underlying type
 * @param deconstruct Function to extract the underlying value from the Value Object
 */
abstract class ValueObjectSerializer<T : Any, V : Any>(
    private val underlyingSerializer: KSerializer<V>,
    private val construct: (V) -> T,
    private val deconstruct: (T) -> V
) : KSerializer<T> {

    override val descriptor: SerialDescriptor = underlyingSerializer.descriptor

    override fun serialize(encoder: Encoder, value: T) {
        underlyingSerializer.serialize(encoder, deconstruct(value))
    }

    override fun deserialize(decoder: Decoder): T {
        // 1. Attempt to deserialize the underlying type (e.g., parse a String into a LocalDate)
        val underlyingValue = try {
            underlyingSerializer.deserialize(decoder)
        } catch (e: Exception) {
            throw DataQualityException("Invalid format for ${descriptor.serialName}.")
        }

        // 2. Attempt to construct the Value Object (triggers the init block validations)
        return try {
            construct(underlyingValue)
        } catch (e: DataQualityException) {
            // Already the correct exception type, throw it directly
            throw e
        } catch (e: Exception) {
            // Map require() failures (IllegalArgumentException) to DataQualityException
            throw DataQualityException(e.message ?: "Validation failed for ${descriptor.serialName}.")
        }
    }
}