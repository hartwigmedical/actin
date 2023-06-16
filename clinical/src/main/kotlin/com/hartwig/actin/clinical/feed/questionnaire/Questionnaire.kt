package com.hartwig.actin.clinical.feed.questionnaire

import com.hartwig.actin.clinical.datamodel.ECG
import com.hartwig.actin.clinical.datamodel.InfectionStatus
import com.hartwig.actin.clinical.datamodel.TumorStage
import java.time.LocalDate

data class Questionnaire(
    val date: LocalDate,
    val tumorLocation: String?,
    val tumorType: String?,
    val biopsyLocation: String?,
    val stage: TumorStage?,
    val treatmentHistoryCurrentTumor: List<String>?,
    val otherOncologicalHistory: List<String>?,
    val secondaryPrimaries: List<String>?,
    val nonOncologicalHistory: List<String>?,
    val hasMeasurableDisease: Boolean?,
    val hasBrainLesions: Boolean?,
    val hasActiveBrainLesions: Boolean?,
    val hasCnsLesions: Boolean?,
    val hasActiveCnsLesions: Boolean?,
    val hasBoneLesions: Boolean?,
    val hasLiverLesions: Boolean?,
    val otherLesions: List<String>?,
    val ihcTestResults: List<String>?,
    val pdl1TestResults: List<String>?,
    val whoStatus: Int?,
    val unresolvedToxicities: List<String>?,
    val infectionStatus: InfectionStatus?,
    val ecg: ECG?,
    val complications: List<String>?,
    val genayaSubjectNumber: String?,
)