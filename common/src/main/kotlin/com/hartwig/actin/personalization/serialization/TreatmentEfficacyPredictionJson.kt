package com.hartwig.actin.personalization.serialization

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import java.io.File
import java.nio.file.Files

object TreatmentEfficacyPredictionJson {

    fun read(treatmentEfficacyPredictionJson: String): Map<String, List<Double>> {
        return fromJson(Files.readString(File(treatmentEfficacyPredictionJson).toPath()))
    }

    fun fromJson(json: String): Map<String, List<Double>> {
        val mapper = ObjectMapper().registerModule(KotlinModule.Builder().build())
            .registerModule(SimpleModule().apply {
                addDeserializer(Map::class.java, PredictionsSerializer())
            })

        return mapper.readValue(json, object : TypeReference<Map<String, List<Double>>>() {})
    }
}

class PredictionsSerializer : JsonDeserializer<Map<String, List<Double>>>() {

    override fun deserialize(parser: JsonParser, context: DeserializationContext): Map<String, List<Double>> {
        val node = parser.readValueAsTree<JsonNode>()
        
        val treatments: MutableList<String> = mutableListOf()
        node.fieldNames().forEach { treatments.add(it) }

        return treatments.associateWith { treatment ->
            val probabilities: MutableList<Double> = mutableListOf()
            node.get(treatment).get("survival_probs").forEach { probability -> probabilities.add(probability.asDouble())}
            probabilities
        }
    }
}