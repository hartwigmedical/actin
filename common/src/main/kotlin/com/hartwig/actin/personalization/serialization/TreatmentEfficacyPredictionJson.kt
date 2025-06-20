package com.hartwig.actin.personalization.serialization

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import java.io.File
import java.nio.file.Files

private data class TreatmentEfficacyPrediction(@JsonProperty("survival_probs") val survivalProbs: List<Double>)

object TreatmentEfficacyPredictionJson {

    private val mapper = ObjectMapper().registerModule(KotlinModule.Builder().build())

    fun read(treatmentEfficacyPredictionJson: String): Map<String, List<Double>> {
        val results: Map<String, TreatmentEfficacyPrediction> =
            mapper.readValue(Files.readString(File(treatmentEfficacyPredictionJson).toPath()))
        
        return results.mapValues { it.value.survivalProbs }
    }
}