package com.hartwig.actin.personalization.serialization

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import org.apache.logging.log4j.LogManager
import java.io.File
import java.nio.file.Files

private data class TreatmentEfficacyPrediction(@JsonProperty("survival_probs") val survivalProbs: List<Double>)

object TreatmentEfficacyPredictionJson {

    private val logger = LogManager.getLogger(TreatmentEfficacyPredictionJson::class.java)
    private val mapper = ObjectMapper().registerModule(KotlinModule.Builder().build())

    fun read(treatmentEfficacyPredictionJson: String): Map<String, List<Double>> {
        logger.info("Loading treatment efficacy predictions from $treatmentEfficacyPredictionJson")
        val results: Map<String, TreatmentEfficacyPrediction> =
            mapper.readValue(Files.readString(File(treatmentEfficacyPredictionJson).toPath()))

        logger.info(" Loaded treatment efficacy predictions for ${results.keys.size} treatments")
        
        return results.mapValues { it.value.survivalProbs }
    }
}