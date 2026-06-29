package com.hartwig.actin.util.json

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonMappingException

class StrictEnumDeserializer<T : Enum<T>>(val enumType: Class<T>) : JsonDeserializer<T>() {

    private val lookup = enumType.enumConstants.groupBy { it.name }

    override fun deserialize(parser: JsonParser, context: DeserializationContext): T {
        val raw = parser.text?.trim()
        val normalized = raw?.uppercase()
        return lookup[normalized]?.firstOrNull()
            ?: throw JsonMappingException.from(parser, "Unknown enum value for type ${enumType.simpleName}: \"$raw\"")
    }
}
