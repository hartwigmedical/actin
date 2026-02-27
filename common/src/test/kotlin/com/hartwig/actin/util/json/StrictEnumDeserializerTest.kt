package com.hartwig.actin.util.json

import com.google.gson.GsonBuilder
import com.google.gson.JsonPrimitive
import com.hartwig.actin.datamodel.clinical.treatment.Radiotherapy
import com.hartwig.actin.datamodel.clinical.treatment.RadiotherapyType
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.Test

class StrictEnumDeserializerTest {

    private val gson = GsonBuilder()
        .registerTypeAdapter(RadiotherapyType::class.java, StrictEnumDeserializer(RadiotherapyType::class.java))
        .create()

    @Test
    fun `Should deserialize valid enum value`() {
        assertThat(gson.fromJson(JsonPrimitive("Brachytherapy"), RadiotherapyType::class.java))
            .isEqualTo(RadiotherapyType.BRACHYTHERAPY)
    }

    @Test
    fun `Should deserialize nulls for nullable fields`() {
        val radiotherapy = gson.fromJson(
            " {\"name\": \"RADIOTHERAPY\", \"synonyms\": [\"Radiation\"], \"isSystemic\": false, \"radioType\": null, "
                    + "\"isInternal\": null, \"treatmentClass\": \"RADIOTHERAPY\"}",
            Radiotherapy::class.java
        )
        assertThat(radiotherapy.radioType).isNull()
    }

    @Test
    fun `Should throw exception for invalid enum value`() {
        assertThatThrownBy { gson.fromJson(JsonPrimitive("INVALID"), RadiotherapyType::class.java) }
            .isInstanceOf(IllegalArgumentException::class.java)
            .hasMessage("Unknown enum value for type RadiotherapyType: \"INVALID\"")
    }
}