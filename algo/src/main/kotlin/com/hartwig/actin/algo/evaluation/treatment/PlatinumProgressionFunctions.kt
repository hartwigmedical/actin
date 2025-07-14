package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.calendar.DateComparison.isAfterDate
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.clinical.treatment.DrugTreatment
import com.hartwig.actin.datamodel.clinical.treatment.DrugType
import com.hartwig.actin.datamodel.clinical.treatment.history.TreatmentHistoryEntry
import java.time.LocalDate

class PlatinumProgressionFunctions(val platinumTreatments: Set<TreatmentHistoryEntry>) {

    fun hasProgressionOrUnknownProgressionOnPlatinum() = platinumTreatments.any { isProgressiveDisease(it) != false }

    fun hasProgressionOnPlatinumWithinSixMonths(referenceDate: LocalDate) = platinumTreatments.filter { isProgressiveDisease(it) == true }
        .any { isAfterDate(referenceDate.minusMonths(6), it.startYear, it.startMonth) == true }

    private fun isProgressiveDisease(entry: TreatmentHistoryEntry) = ProgressiveDiseaseFunctions.treatmentResultedInPD(entry)

    companion object {
        fun create(record: PatientRecord): PlatinumProgressionFunctions {
            val treatments = record.oncologicalHistory.asSequence().filter { entry ->
                entry.allTreatments().filterIsInstance<DrugTreatment>()
                    .any { treatment -> treatment.drugs.any { it.drugTypes.contains(DrugType.PLATINUM_COMPOUND) } }
            }.toSet()
            return PlatinumProgressionFunctions(treatments)
        }
    }
}