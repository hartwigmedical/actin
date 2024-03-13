package com.hartwig.actin.trial.nki

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.hartwig.actin.trial.ctc.config.CTCDatabase
import com.hartwig.actin.trial.ctc.config.CTCDatabaseEntry
import java.io.File

object NKIDatabaseReader {

    private val mapper = jacksonObjectMapper().apply {
        disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
        setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
        registerModule(JavaTimeModule())
        registerModule(KotlinModule.Builder().build())
    }

    fun read(nkiConfigDirectory: String): CTCDatabase {

        return CTCDatabase(
            mapper.readValue(File(nkiConfigDirectory), object : TypeReference<List<NKITrialStatus>>() {})
                .map {
                    CTCDatabaseEntry(
                        studyId = it.studyId.toInt(),
                        studyMETC = it.studyMetc,
                        studyAcronym = it.studyAcronym,
                        studyTitle = it.studyTitle,
                        studyStatus = it.studyStatus
                    )
                }, emptySet(), emptySet()
        )
    }
}