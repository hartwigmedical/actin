package com.hartwig.actin.clinical.feed.treatment

import com.hartwig.actin.clinical.sort.TreatmentHistoryAscendingDateComparator
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentCategory
import com.hartwig.actin.datamodel.clinical.treatment.history.TreatmentHistoryDetails
import com.hartwig.actin.datamodel.clinical.treatment.history.TreatmentHistoryEntry
import java.time.LocalDate

object TreatmentHistoryEntryFunctions {

    fun setMaxStopDate(treatmentHistory: List<TreatmentHistoryEntry>): List<TreatmentHistoryEntry> {
        val (systemic, nonSystemic) = treatmentHistory.sortedWith(TreatmentHistoryAscendingDateComparator()).partition {
            it.categories().all(TreatmentCategory.SYSTEMIC_CANCER_TREATMENT_CATEGORIES::contains)
        }

        val systemicWithStopDate = systemic.mapIndexed { index, current ->
            if (current.treatmentHistoryDetails?.stopYear == null) {
                val treatmentDetails = if (index < systemic.size - 1) {
                    val next = systemic[index + 1]
                    TreatmentHistoryDetails(maxStopYear = next.startYear, maxStopMonth = next.startMonth)
                } else {
                    TreatmentHistoryDetails(maxStopYear = LocalDate.now().year, maxStopMonth = LocalDate.now().monthValue)
                }
                current.copy(treatmentHistoryDetails = treatmentDetails)
            } else current
        }

        return systemicWithStopDate + nonSystemic
    }
}