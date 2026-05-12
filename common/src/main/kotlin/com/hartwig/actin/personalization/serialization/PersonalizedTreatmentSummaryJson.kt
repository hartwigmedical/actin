package com.hartwig.actin.personalization.serialization

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import com.hartwig.actin.datamodel.algo.PersonalizedTreatmentSummary
import io.github.oshai.kotlinlogging.KotlinLogging
import java.io.File
import java.nio.file.Files


object PersonalizedTreatmentSummaryJson {

    private val logger = KotlinLogging.logger {}
    private val mapper = ObjectMapper().registerModule(KotlinModule.Builder().build())

    fun read(personalizedTreatmentSummary: String): PersonalizedTreatmentSummary {
        logger.info { "Loading treatment efficacy predictions from $personalizedTreatmentSummary" }

        val json = Files.readString(File(personalizedTreatmentSummary).toPath())
        val results = mapper.readValue<PersonalizedTreatmentSummary>(json)

        logger.info { " Loaded treatment efficacy predictions for ${results.predictions?.size} treatments" }

        return results
    }
}