package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.calendar.DateComparison.isAfterDate
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.clinical.treatment.DrugType
import com.hartwig.actin.datamodel.clinical.treatment.history.TreatmentHistoryEntry
import java.time.LocalDate

class PlatinumProgressionAnalysis(
    val firstPlatinumTreatment: TreatmentHistoryEntry?,
    val lastPlatinumTreatment: TreatmentHistoryEntry?,
    val referenceDate: LocalDate
) {

    fun hasProgressionOrUnknownProgressionOnFirstPlatinum() = hasProgressionOrUnknownProgressionOnPlatinum(firstPlatinumTreatment)

    fun hasProgressionOrUnknownProgressionOnLastPlatinum() = hasProgressionOrUnknownProgressionOnPlatinum(lastPlatinumTreatment)

    fun hasProgressionOnFirstPlatinumWithinMonths(minMonths: Int) = hasProgressionOnPlatinumWithinMonths(firstPlatinumTreatment, minMonths)

    fun hasProgressionOnLastPlatinumWithinSixMonths() = hasProgressionOnPlatinumWithinMonths(lastPlatinumTreatment, 6)

    private fun hasProgressionOrUnknownProgressionOnPlatinum(platinumTreatment: TreatmentHistoryEntry?) =
        platinumTreatment?.let { isProgressiveDisease(it) != false }

    private fun hasProgressionOnPlatinumWithinMonths(platinumTreatment: TreatmentHistoryEntry?, minMonths: Int): Boolean? =
        platinumTreatment?.let {
            isProgressiveDisease(it) == true && isAfterDate(
                referenceDate.minusMonths(minMonths.toLong()),
                it.startYear,
                it.startMonth
            ) == true
        }

    private fun isProgressiveDisease(entry: TreatmentHistoryEntry?) = entry?.let(ProgressiveDiseaseFunctions::treatmentResultedInPD)

    companion object {
        fun create(record: PatientRecord, referenceDate: LocalDate): PlatinumProgressionAnalysis {
            val platinumTreatments = record.oncologicalHistory.filter { it.isOfType(DrugType.PLATINUM_COMPOUND) == true }
            val lastPlatinumTreatment = SystemicTreatmentAnalyser.lastSystemicTreatment(platinumTreatments)
            val firstPlatinumTreatment = SystemicTreatmentAnalyser.firstSystemicTreatment(platinumTreatments)
            return PlatinumProgressionAnalysis(firstPlatinumTreatment, lastPlatinumTreatment, referenceDate)
        }
    }
}