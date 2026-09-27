package com.hartwig.actin.util.json

import com.fasterxml.jackson.databind.JsonMappingException
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import com.hartwig.actin.datamodel.clinical.treatment.RadiotherapyType
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test

class StrictEnumDeserializerTest {

    private val mapper: ObjectMapper = ObjectMapper().registerModule(
        SimpleModule().addDeserializer(RadiotherapyType::class.java, StrictEnumDeserializer(RadiotherapyType::class.java))
    )

    @Test
    fun `Should deserialize valid enum value`() {
        assertThat(mapper.readValue("\"Brachytherapy\"", RadiotherapyType::class.java))
            .isEqualTo(RadiotherapyType.BRACHYTHERAPY)
    }

    @Test
    fun `Should throw exception for invalid enum value`() {
        assertThatThrownBy { mapper.readValue("\"INVALID\"", RadiotherapyType::class.java) }
            .isInstanceOf(JsonMappingException::class.java)
            .hasMessageContaining("Unknown enum value for type RadiotherapyType: \"INVALID\"")
    }

    @Test
    fun `Should deserialize JSON null to null for nullable enum field`() {
        val kotlinMapper = ActinObjectMapper.create().registerModule(
            SimpleModule().addDeserializer(RadiotherapyType::class.java, StrictEnumDeserializer(RadiotherapyType::class.java))
        )
        val wrapper = kotlinMapper.readValue("""{"type":null}""", NullableEnumWrapper::class.java)
        assertThat(wrapper.type).isNull()
    }

    private data class NullableEnumWrapper(val type: RadiotherapyType?)
}
