package domain.models.valueobjects

import core.exceptions.DataQualityException
import core.serialization.ValueObjectSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.serializer
import java.math.BigInteger

private val alphabetPositions = mapOf(
    'A' to 10,
    'B' to 11,
    'C' to 12,
    'D' to 13,
    'E' to 14,
    'F' to 15,
    'G' to 16,
    'H' to 17,
    'I' to 18,
    'J' to 19,
    'K' to 20,
    'L' to 21,
    'M' to 22,
    'N' to 23,
    'O' to 24,
    'P' to 25,
    'Q' to 26,
    'R' to 27,
    'S' to 28,
    'T' to 29,
    'U' to 30,
    'V' to 31,
    'W' to 32,
    'X' to 33,
    'Y' to 34,
    'Z' to 35
)

private fun alphaNumericToNumber(alphaNumeric: String): BigInteger =
    alphaNumeric.map { it.digitToIntOrNull() ?: alphabetPositions[it]!! }.joinToString("").toBigInteger()


@JvmInline
@Serializable(with = IBANSerializer::class)
value class IBAN(val value: String) {
    init {
        val normalized = value.replace(" ", "")

        // Basic IBAN Regex validation (Example: starts with 2 letters, 2 digits, then alphanumeric)
        if (!normalized.matches(Regex("^[A-Z]{2}[0-9]{2}[A-Z0-9]{1,30}$"))) {
            throw DataQualityException("Invalid IBAN format")
        }

        // https://en.wikipedia.org/wiki/International_Bank_Account_Number#:~:text=Validating%20the%20IBAN%5Bedit%5D
        val countryCode = normalized.substring(0..1)
        val checksum = normalized.substring(2..3).toInt()

        // Rearrange: Move Country Code + Check to back
        val reordered = alphaNumericToNumber("${normalized.substring(4)}$countryCode$checksum")
        val valid = reordered.mod(97.toBigInteger()).compareTo(BigInteger.ONE) == 0
        if (!valid) {
            throw DataQualityException("This IBAN is not valid!")
        }
    }
}

object IBANSerializer : ValueObjectSerializer<IBAN, String>(
    underlyingSerializer = String.serializer(),
    construct = ::IBAN,
    deconstruct = IBAN::value
)