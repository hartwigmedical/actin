package com.hartwig.actin.clinical.serialization

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonMappingException
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.core.JsonGenerator
import com.hartwig.actin.datamodel.clinical.Comorbidity
import com.hartwig.actin.datamodel.clinical.WhoStatus
import com.hartwig.actin.datamodel.clinical.treatment.Drug
import com.hartwig.actin.datamodel.clinical.treatment.DrugType
import com.hartwig.actin.datamodel.clinical.treatment.OtherTreatmentType
import com.hartwig.actin.datamodel.clinical.treatment.RadiotherapyType
import com.hartwig.actin.datamodel.clinical.treatment.Treatment
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentCategory
import com.hartwig.actin.datamodel.molecular.driver.CopyNumberType
import com.hartwig.actin.util.json.ActinObjectMapper
import com.hartwig.actin.util.json.StrictEnumDeserializer

object ClinicalRecordJsonMapper {

    fun create(): ObjectMapper {
        return ActinObjectMapper.create().registerModule(clinicalAdaptersModule())
    }

    fun createWithDrugMap(drugsByName: Map<String, Drug>): ObjectMapper {
        return ActinObjectMapper.create().registerModule(
            clinicalAdaptersModule().apply {
                addDeserializer(Drug::class.java, DrugNameDeserializer(drugsByName))
                addSerializer(Drug::class.java, DrugNameSerializer)
            }
        )
    }

    private fun clinicalAdaptersModule(): SimpleModule {
        return SimpleModule().apply {
            addDeserializer(Treatment::class.java, TreatmentDeserializer)
            addDeserializer(Comorbidity::class.java, ComorbidityDeserializer)
            addDeserializer(WhoStatus::class.java, WhoStatusDeserializer)
            addDeserializer(Drug::class.java, DrugDeserializer)
            addDeserializer(DrugType::class.java, StrictEnumDeserializer(DrugType::class.java))
            addDeserializer(OtherTreatmentType::class.java, StrictEnumDeserializer(OtherTreatmentType::class.java))
            addDeserializer(RadiotherapyType::class.java, StrictEnumDeserializer(RadiotherapyType::class.java))
            addDeserializer(TreatmentCategory::class.java, StrictEnumDeserializer(TreatmentCategory::class.java))
            addDeserializer(CopyNumberType::class.java, StrictEnumDeserializer(CopyNumberType::class.java))
        }
    }

    private class DrugNameDeserializer(private val drugsByName: Map<String, Drug>) : JsonDeserializer<Drug>() {

        override fun deserialize(parser: JsonParser, context: DeserializationContext): Drug {
            if (parser.currentToken() != JsonToken.VALUE_STRING) {
                throw JsonMappingException.from(parser, "Expected string drug name, got token ${parser.currentToken()}")
            }
            val name = parser.text
            return drugsByName[name.lowercase()]
                ?: throw JsonMappingException.from(parser, "Failed to resolve drug name: $name")
        }
    }

    private object DrugNameSerializer : JsonSerializer<Drug>() {

        override fun serialize(value: Drug, gen: JsonGenerator, serializers: SerializerProvider) {
            gen.writeString(value.name)
        }
    }
}
