package com.hartwig.actin.clinical.serialization

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import com.hartwig.actin.datamodel.clinical.treatment.Treatment
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentClass
import com.hartwig.actin.util.json.Json
import java.lang.reflect.Type

class TreatmentAdapter : JsonDeserializer<Treatment?> {

    override fun deserialize(jsonElement: JsonElement, type: Type, context: JsonDeserializationContext): Treatment? {
        return try {
            if (jsonElement.isJsonNull) null else {
                context.deserialize<Any>(
                    jsonElement, TreatmentClass.valueOf(Json.string(jsonElement.asJsonObject, "treatmentClass")).treatmentClass
                ) as Treatment
            }
        } catch (e: Exception) {
            throw JsonParseException("Failed to deserialize: $jsonElement", e)
        }
    }
}