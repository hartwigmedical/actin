package com.hartwig.actin.trial.status.nki

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.hartwig.actin.trial.status.TrialStatus.CLOSED
import com.hartwig.actin.trial.status.TrialStatus.OPEN
import com.hartwig.actin.trial.status.TrialStatusEntry
import com.hartwig.actin.trial.status.TrialStatusEntryReader
import java.io.File

private const val TRIALS_JSON = "trial_cohort_status.json"
private const val NKI_OPEN_STATUS = "OPEN"
private val STATUSES_TO_INCLUDE = setOf(NKI_OPEN_STATUS, "CLOSED", "SUSPENDED")
private const val ALWAYS_ASSUME_OPEN_SLOTS_FOR_NKI = 1

class NKITrialStatusEntryReader : TrialStatusEntryReader {

    private val mapper = jacksonObjectMapper().apply {
        disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
        registerModule(JavaTimeModule())
        registerModule(KotlinModule.Builder().build())
    }

    override fun read(inputPath: String): List<TrialStatusEntry> {
        return mapper.readValue(File("$inputPath/$TRIALS_JSON"), object : TypeReference<List<NKITrialStatus>>() {})
            .filter { it.studyStatus != null && it.studyMetc != null }
            .filter { it.studyStatus in STATUSES_TO_INCLUDE }
            .map {
                TrialStatusEntry(
                    studyId = it.studyId.toInt(),
                    metcStudyID = it.studyMetc!!,
                    studyAcronym = it.studyAcronym,
                    studyTitle = it.studyTitle,
                    studyStatus = if (it.studyStatus == NKI_OPEN_STATUS) OPEN else CLOSED,
                    cohortId = it.cohortId,
                    cohortStatus = if (it.cohortOpen == true) OPEN else CLOSED,
                    cohortSlotsNumberAvailable = ALWAYS_ASSUME_OPEN_SLOTS_FOR_NKI
                )
            }
    }
}