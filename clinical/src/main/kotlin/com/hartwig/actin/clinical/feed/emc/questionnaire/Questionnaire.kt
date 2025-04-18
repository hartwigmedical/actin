package com.hartwig.actin.clinical.feed.emc.questionnaire

import com.hartwig.actin.datamodel.clinical.Ecg
import com.hartwig.actin.datamodel.clinical.InfectionStatus
import com.hartwig.actin.datamodel.clinical.TumorStage
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
    val ecg: Ecg?,
    val complications: List<String>?
) {
    fun isEmpty(): Boolean {
        val allStringsEmpty = listOf(
            tumorLocation,
            tumorType,
            biopsyLocation
        ).all { it.isNullOrEmpty() }

        val allListEmpty = listOf(
            treatmentHistoryCurrentTumor,
            otherOncologicalHistory,
            secondaryPrimaries,
            nonOncologicalHistory,
            otherLesions,
            ihcTestResults,
            pdl1TestResults,
            unresolvedToxicities,
            complications
        ).all { it.isNullOrEmpty() }

        val allBooleanAndObjectFieldsNull = listOf(
            stage,
            hasMeasurableDisease,
            hasBrainLesions,
            hasActiveBrainLesions,
            hasCnsLesions,
            hasActiveCnsLesions,
            hasBoneLesions,
            hasLiverLesions,
            whoStatus,
            infectionStatus,
            ecg
        ).all { it == null }
        return allStringsEmpty && allListEmpty && allBooleanAndObjectFieldsNull
    }
}