package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.calendar.DateComparison.isAfterDate
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.clinical.treatment.DrugTreatment
import com.hartwig.actin.datamodel.clinical.treatment.DrugType
import com.hartwig.actin.datamodel.clinical.treatment.history.TreatmentHistoryEntry
import java.time.LocalDate

class PlatinumProgressionFunctions(val platinumTreatment: TreatmentHistoryEntry?) {

    fun hasProgressionOrUnknownProgressionOnPlatinum() = platinumTreatment?.let { isProgressiveDisease(it) != false } ?: false

    fun hasProgressionOnPlatinumWithinSixMonths(referenceDate: LocalDate) =
        isProgressiveDisease(platinumTreatment) == true && platinumTreatment?.let {
            isAfterDate(referenceDate.minusMonths(6), it.startYear, it.startMonth) == true
        } == true

    private fun isProgressiveDisease(entry: TreatmentHistoryEntry?) = entry?.let(ProgressiveDiseaseFunctions::treatmentResultedInPD)

    companion object {
        fun create(record: PatientRecord): PlatinumProgressionFunctions {
            val platinumTreatments = record.oncologicalHistory.asSequence().filter { entry ->
                entry.allTreatments().filterIsInstance<DrugTreatment>()
                    .any { treatment -> treatment.drugs.any { it.drugTypes.contains(DrugType.PLATINUM_COMPOUND) } }
            }.toList()
            val lastPlatinumTreatment = SystemicTreatmentAnalyser.lastSystemicTreatment(platinumTreatments)
            return PlatinumProgressionFunctions(lastPlatinumTreatment)
        }
    }
}