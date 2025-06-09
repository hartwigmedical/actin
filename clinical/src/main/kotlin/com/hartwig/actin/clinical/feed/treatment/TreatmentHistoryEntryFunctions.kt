package com.hartwig.actin.clinical.feed.treatment

import com.hartwig.actin.clinical.sort.TreatmentHistoryAscendingDateComparator
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentCategory
import com.hartwig.actin.datamodel.clinical.treatment.history.TreatmentHistoryDetails
import com.hartwig.actin.datamodel.clinical.treatment.history.TreatmentHistoryEntry

object TreatmentHistoryEntryFunctions {

    fun setMaxStopDate(treatmentHistory: List<TreatmentHistoryEntry>): List<TreatmentHistoryEntry> {
        val (systemic, nonSystemic) = treatmentHistory.sortedWith(TreatmentHistoryAscendingDateComparator()).partition {
            it.categories().all(TreatmentCategory.SYSTEMIC_CANCER_TREATMENT_CATEGORIES::contains)
        }

        val systemicWithStopDate = systemic.mapIndexed { index, current ->
            if (current.treatmentHistoryDetails?.stopYear == null && index < systemic.size - 1) {
                val next = systemic[index + 1]
                current.copy(
                    treatmentHistoryDetails = TreatmentHistoryDetails(
                        maxStopYear = next.startYear,
                        maxStopMonth = next.startMonth
                    )
                )
            } else current
        }

        return systemicWithStopDate + nonSystemic
    }
}