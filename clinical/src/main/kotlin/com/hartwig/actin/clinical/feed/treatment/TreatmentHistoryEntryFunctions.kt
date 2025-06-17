package com.hartwig.actin.clinical.feed.treatment

import com.hartwig.actin.clinical.sort.TreatmentHistoryAscendingDateComparator
import com.hartwig.actin.datamodel.clinical.treatment.history.TreatmentHistoryDetails
import com.hartwig.actin.datamodel.clinical.treatment.history.TreatmentHistoryEntry
import java.time.LocalDate

object TreatmentHistoryEntryFunctions {

    private const val LAST_MONTH = 12

    fun setMaxStopDate(treatmentHistory: List<TreatmentHistoryEntry>, date: LocalDate): List<TreatmentHistoryEntry> {
        val (systemic, nonSystemic) = treatmentHistory.sortedWith(TreatmentHistoryAscendingDateComparator()).partition {
            it.treatments.any { treatment -> treatment.isSystemic }
        }

        val maxDate = date.plusMonths(1)

        val systemicWithStopDate = systemic.mapIndexed { index, current ->
            val details = current.treatmentHistoryDetails

            val updatedDetails = when {
                details?.stopYear != null && details.stopMonth == null -> {
                    val maxStopMonth = if (index < systemic.size - 1) {
                        val next = systemic[index + 1]
                        if (next.startYear == details.stopYear) next.startMonth ?: LAST_MONTH else LAST_MONTH
                    } else {
                        LAST_MONTH
                    }
                    details.copy(maxStopMonth = maxStopMonth)
                }

                details?.stopYear == null -> {
                    val (maxStopYear, maxStopMonth) = if (index < systemic.size - 1) {
                        val next = systemic[index + 1]
                        next.startYear to next.startMonth
                    } else {
                        maxDate.year to maxDate.monthValue
                    }
                    details?.copy(maxStopYear = maxStopYear, maxStopMonth = maxStopMonth)
                        ?: TreatmentHistoryDetails(maxStopYear = maxStopYear, maxStopMonth = maxStopMonth)
                }

                else -> details
            }

            current.copy(treatmentHistoryDetails = updatedDetails)
        }

        return systemicWithStopDate + nonSystemic
    }
}