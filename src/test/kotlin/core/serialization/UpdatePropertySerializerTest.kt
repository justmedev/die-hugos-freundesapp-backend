package core.serialization

import core.utils.UpdateProperty
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*
import kotlin.test.Test
import kotlin.test.assertEquals


class UpdatePropertySerializerTest {
    @Serializable
    private data class SampleCommand(
        val username: UpdateProperty<String> = UpdateProperty(),
        val age: UpdateProperty<Int?> = UpdateProperty(),
        val likesCheese: UpdateProperty<Boolean> = UpdateProperty(),
    )

    @Test
    fun `deserialize into partial object with update properties`() {
        val jsonObject: JsonObject = buildJsonObject {
            put("username", "test")
            put("age", JsonNull) // Explicitly setting to null
            // likesCheese is completely omitted
        }

        val decoded = Json.decodeFromJsonElement<SampleCommand>(jsonObject)

        assertEquals(true, decoded.username.update)
        assertEquals("test", decoded.username.value)
        assertEquals(true, decoded.age.update)
        assertEquals(null, decoded.age.value)
        assertEquals(false, decoded.likesCheese.update)
    }
}