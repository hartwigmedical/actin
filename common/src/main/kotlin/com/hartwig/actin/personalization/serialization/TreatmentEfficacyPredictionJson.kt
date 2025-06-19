package com.hartwig.actin.personalization.serialization

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import java.io.File
import java.nio.file.Files

data class TreatmentEfficacyPrediction(val survivalProbs: List<Double>)

object TreatmentEfficacyPredictionJson {
    private val mapper = ObjectMapper().registerModule(KotlinModule.Builder().build())

    fun read(treatmentEfficacyPredictionJson: String): Map<String, TreatmentEfficacyPrediction> {
        return mapper.readValue(Files.readString(File(treatmentEfficacyPredictionJson).toPath()))
    }
}