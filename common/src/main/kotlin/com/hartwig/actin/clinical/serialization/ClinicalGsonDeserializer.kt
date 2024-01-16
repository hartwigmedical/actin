package com.hartwig.actin.clinical.serialization

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonParseException
import com.hartwig.actin.clinical.datamodel.treatment.Drug
import com.hartwig.actin.clinical.datamodel.treatment.Treatment
import com.hartwig.actin.clinical.datamodel.treatment.TreatmentClass
import com.hartwig.actin.util.json.GsonLocalDateTimeAdapter
import com.hartwig.actin.util.json.Json.integer
import com.hartwig.actin.util.json.Json.string
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
        @Throws(JsonParseException::class)
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

    private class TreatmentAdapter : JsonDeserializer<Treatment?> {

        @Throws(JsonParseException::class)
        override fun deserialize(jsonElement: JsonElement, type: Type, context: JsonDeserializationContext): Treatment? {
            return try {
                if (jsonElement.isJsonNull) null else {
                    context.deserialize<Any>(
                        jsonElement, TreatmentClass.valueOf(string(jsonElement.asJsonObject, "treatmentClass")).treatmentClass
                    ) as Treatment
                }
            } catch (e: Exception) {
                throw JsonParseException("Failed to deserialize: $jsonElement", e)
            }
        }
    }
}
