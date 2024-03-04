package com.hartwig.actin.clinical.serialization

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import com.hartwig.actin.clinical.datamodel.treatment.Drug
import com.hartwig.actin.clinical.datamodel.treatment.Treatment
import com.hartwig.actin.util.json.GsonLocalDateTimeAdapter
import com.hartwig.actin.util.json.Json.integer
import java.lang.reflect.Type
import java.time.LocalDate
import java.time.LocalDateTime

object ClinicalGsonDeserializer {

    fun create(): Gson {
        return gsonBuilder().create()
    }

    fun createWithDrugMap(drugsByName: Map<String, Drug>): Gson {
        return gsonBuilder().registerTypeAdapter(Drug::class.java, DrugNameAdapter(drugsByName)).create()
    }

    private fun gsonBuilder(): GsonBuilder {
        return GsonBuilder().serializeNulls()
            .enableComplexMapKeySerialization()
            .registerTypeAdapter(LocalDate::class.java, LocalDateAdapter())
            .registerTypeAdapter(LocalDateTime::class.java, GsonLocalDateTimeAdapter())
            .registerTypeAdapter(Treatment::class.java, TreatmentAdapter())
    }

    private class LocalDateAdapter : JsonDeserializer<LocalDate?> {

        override fun deserialize(
            jsonElement: JsonElement, type: Type,
            jsonDeserializationContext: JsonDeserializationContext
        ): LocalDate? {
            return if (jsonElement.isJsonNull) {
                null
            } else {
                val dateObject = jsonElement.asJsonObject
                LocalDate.of(integer(dateObject, "year"), integer(dateObject, "month"), integer(dateObject, "day"))
            }
        }
    }

    private class DrugNameAdapter(private val drugsByName: Map<String, Drug>) : JsonDeserializer<Drug> {

        override fun deserialize(json: JsonElement, type: Type, context: JsonDeserializationContext): Drug {
            return drugsByName[json.asString.lowercase()]
                ?: throw JsonParseException("Failed to resolve: $json")
        }
    }
}
