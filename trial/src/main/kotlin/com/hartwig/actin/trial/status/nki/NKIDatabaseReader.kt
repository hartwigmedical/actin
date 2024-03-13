package com.hartwig.actin.trial.status.nki

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.hartwig.actin.trial.status.TrialStatus
import com.hartwig.actin.trial.status.TrialStatusDatabase
import com.hartwig.actin.trial.status.TrialStatusEntry
import java.io.File

object NKIDatabaseReader {

    private val mapper = jacksonObjectMapper().apply {
        disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
        setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
        registerModule(JavaTimeModule())
        registerModule(KotlinModule.Builder().build())
    }

    fun read(nkiConfigDirectory: String): TrialStatusDatabase {

        return TrialStatusDatabase(
            mapper.readValue(File(nkiConfigDirectory), object : TypeReference<List<NKITrialStatus>>() {})
                .map {
                    TrialStatusEntry(
                        studyId = it.studyId.toInt(),
                        studyMETC = it.studyMetc,
                        studyAcronym = it.studyAcronym,
                        studyTitle = it.studyTitle,
                        studyStatus = if (it.studyStatus == "OPEN") TrialStatus.OPEN else if (it.studyStatus == "CLOSED") TrialStatus.CLOSED else TrialStatus.UNINTERPRETABLE,
                    )
                }, emptySet(), emptySet()
        )
    }
}