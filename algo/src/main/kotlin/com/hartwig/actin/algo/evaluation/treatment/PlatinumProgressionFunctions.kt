package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.calendar.DateComparison.isAfterDate
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.clinical.treatment.DrugType
import com.hartwig.actin.datamodel.clinical.treatment.history.TreatmentHistoryEntry
import java.time.LocalDate

class PlatinumProgressionFunctions(
    val firstPlatinumTreatment: TreatmentHistoryEntry?,
    val lastPlatinumTreatment: TreatmentHistoryEntry?,
    val referenceDate: LocalDate
) {

    fun hasProgressionOrUnknownProgressionOnFirstPlatinum() = hasProgressionOrUnknownProgressionOnPlatinum(firstPlatinumTreatment)

    fun hasProgressionOrUnknownProgressionOnLastPlatinum() = hasProgressionOrUnknownProgressionOnPlatinum(lastPlatinumTreatment)

    fun hasProgressionOnFirstPlatinumWithinMonths(minMonths: Int) = hasProgressionOnPlatinumWithinMonths(firstPlatinumTreatment, minMonths)

    fun hasProgressionOnLastPlatinumWithinSixMonths() = hasProgressionOnPlatinumWithinMonths(lastPlatinumTreatment, 6)

    private fun hasProgressionOrUnknownProgressionOnPlatinum(platinumTreatment: TreatmentHistoryEntry?) =
        platinumTreatment?.let { isProgressiveDisease(it) != false } ?: false

    private fun hasProgressionOnPlatinumWithinMonths(platinumTreatment: TreatmentHistoryEntry?, minMonths: Int) =
        isProgressiveDisease(platinumTreatment) == true && platinumTreatment?.let {
            isAfterDate(
                referenceDate.minusMonths(minMonths.toLong()),
                it.startYear,
                it.startMonth
            )
        } == true

    private fun isProgressiveDisease(entry: TreatmentHistoryEntry?) = entry?.let(ProgressiveDiseaseFunctions::treatmentResultedInPD)

    companion object {
        fun create(record: PatientRecord, referenceDate: LocalDate): PlatinumProgressionFunctions {
            val platinumTreatments = record.oncologicalHistory.filter { it.isOfType(DrugType.PLATINUM_COMPOUND) == true }
            val lastPlatinumTreatment = SystemicTreatmentAnalyser.lastSystemicTreatment(platinumTreatments)
            val firstPlatinumTreatment = SystemicTreatmentAnalyser.firstSystemicTreatment(platinumTreatments)
            return PlatinumProgressionFunctions(firstPlatinumTreatment, lastPlatinumTreatment, referenceDate)
        }
    }
}