package com.hartwig.actin.util.json

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import java.lang.reflect.Type

class StrictEnumDeserializer<T : Enum<T>>(private val enumType: Class<T>) : JsonDeserializer<T> {

    override fun deserialize(jsonElement: JsonElement, type: Type, context: JsonDeserializationContext): T {
        return enumType.enumConstants.firstOrNull { it.name == jsonElement.asString }
            ?: throw IllegalArgumentException("Unknown enum value for type ${enumType.simpleName}: $jsonElement")
    }
}