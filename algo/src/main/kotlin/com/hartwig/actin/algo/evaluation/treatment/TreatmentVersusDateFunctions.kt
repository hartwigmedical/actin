package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.calendar.DateComparison
import com.hartwig.actin.datamodel.clinical.treatment.history.TreatmentHistoryEntry
import java.time.LocalDate

object TreatmentVersusDateFunctions {

    fun certainTreatmentSinceMinDate(treatment: TreatmentHistoryEntry, minDate: LocalDate): Boolean {
        return DateComparison.isAfterDate(
            minDate,
            treatment.treatmentHistoryDetails?.stopYear,
            treatment.treatmentHistoryDetails?.stopMonth
        )
            ?: DateComparison.isAfterDate(minDate, treatment.startYear, treatment.startMonth)
            ?: false
    }

    fun potentialTreatmentSinceMinDate(treatment: TreatmentHistoryEntry, minDate: LocalDate): Boolean {
        return DateComparison.isAfterDate(minDate, treatment.stopYear(), treatment.stopMonth()) != false
    }

    fun partitionTreatmentsByCertainOccurrenceSinceMinDate(
        treatments: List<TreatmentHistoryEntry>,
        minDate: LocalDate
    ): Pair<List<TreatmentHistoryEntry>, List<TreatmentHistoryEntry>> {
        return treatments.partition { certainTreatmentSinceMinDate(it, minDate) }
    }

    fun certainTreatmentBeforeMaxDate(treatment: TreatmentHistoryEntry, maxDate: LocalDate): Boolean {
        return DateComparison.isBeforeDate(maxDate, treatment.startYear, treatment.startMonth) ?: DateComparison.isBeforeDate(
            maxDate,
            treatment.treatmentHistoryDetails?.stopYear,
            treatment.treatmentHistoryDetails?.stopMonth
        )
        ?: false
    }

    fun potentialTreatmentBeforeMaxDate(treatment: TreatmentHistoryEntry, maxDate: LocalDate): Boolean {
        return DateComparison.isBeforeDate(maxDate, treatment.startYear, treatment.startMonth) != false
    }
}