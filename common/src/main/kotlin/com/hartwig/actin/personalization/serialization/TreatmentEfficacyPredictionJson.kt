package com.hartwig.actin.personalization.serialization

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import com.hartwig.actin.datamodel.algo.TreatmentEfficacyPrediction
import org.apache.logging.log4j.LogManager
import java.io.File
import java.nio.file.Files


object TreatmentEfficacyPredictionJson {

    private val logger = LogManager.getLogger(TreatmentEfficacyPredictionJson::class.java)
    private val mapper = ObjectMapper().registerModule(KotlinModule.Builder().build())

    fun read(treatmentEfficacyPredictionJson: String): Map<String, TreatmentEfficacyPrediction> {
        logger.info("Loading treatment efficacy predictions from $treatmentEfficacyPredictionJson")

        val json = Files.readString(File(treatmentEfficacyPredictionJson).toPath())
        val results = mapper.readValue<Map<String, TreatmentEfficacyPrediction>>(json)

        logger.info(" Loaded treatment efficacy predictions for ${results.keys.size} treatments")

        return results
    }
}