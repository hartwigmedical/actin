package com.hartwig.actin.clinical.serialization

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import com.hartwig.actin.datamodel.clinical.Comorbidity
import com.hartwig.actin.datamodel.clinical.ComorbidityClass
import com.hartwig.actin.util.json.Json
import java.lang.reflect.Type

class ComorbidityAdapter : JsonDeserializer<Comorbidity> {

    override fun deserialize(jsonElement: JsonElement, type: Type, context: JsonDeserializationContext): Comorbidity {
        return try {
            context.deserialize<Any>(
                jsonElement, ComorbidityClass.valueOf(Json.string(jsonElement.asJsonObject, "comorbidityClass")).treatmentClass
            ) as Comorbidity
        } catch (e: Exception) {
            throw JsonParseException("Failed to deserialize: $jsonElement", e)
        }
    }
}