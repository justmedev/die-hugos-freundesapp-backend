package domain.models.valueobjects

import core.exceptions.DataQualityException
import org.junit.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull

class IBANTest {

    @Test
    fun `IBAN - valid - success`() {
        // Germany, valid checksum
        val iban = IBAN("DE36000000000000000000")
        assertNotNull(iban.value)
    }

    @Test
    fun `IBAN - invalid format - throws DataQualityException`() {
        // Too short, invalid characters
        assertFailsWith<DataQualityException>("Invalid IBAN format") {
            IBAN("NOT-AN-IBAN")
        }
    }

    @Test
    fun `IBAN - invalid checksum - throws DataQualityException`() {
        // Germany format, but checksum 37 (should be 36 for all zeros)
        assertFailsWith<DataQualityException>("This IBAN is not valid!") {
            IBAN("DE37000000000000000000")
        }
    }

    @Test
    fun `IBAN - normalized spaces - success`() {
        // IBAN with spaces should be normalized and valid
        val iban = IBAN("DE36 0000 0000 0000 0000 00")
        assertNotNull(iban.value)
    }
}
