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

    fun treatmentBeforeMaxDate(treatment: TreatmentHistoryEntry, maxDate: LocalDate, includeUnknown: Boolean): Boolean {
        // The maxStopDate might be more recent than the actual stopDate. Using stopYear() and stopMonth() here could result
        // in incorrect evaluation to 'false'
        return DateComparison.isBeforeDate(
            maxDate,
            treatment.treatmentHistoryDetails?.stopYear,
            treatment.treatmentHistoryDetails?.stopMonth
        )
            ?: includeUnknown
    }
}