package com.hartwig.actin.clinical.feed.treatment

import com.hartwig.actin.calendar.DateComparison.isBeforeDate
import com.hartwig.actin.calendar.DateComparison.isExactYearAndMonth
import com.hartwig.actin.clinical.sort.TreatmentHistoryAscendingDateComparator
import com.hartwig.actin.datamodel.clinical.treatment.history.TreatmentHistoryDetails
import com.hartwig.actin.datamodel.clinical.treatment.history.TreatmentHistoryEntry
import java.time.LocalDate
import java.time.YearMonth

object TreatmentHistoryEntryFunctions {

    private const val LAST_MONTH = 12
    private const val BUFFER_MONTHS_AFTER_LAST_TREATMENT_DATE: Long = 1

    fun setMaxStopDate(
        treatmentHistory: List<TreatmentHistoryEntry>,
        questionnaireDate: LocalDate?,
        registrationDate: LocalDate
    ): List<TreatmentHistoryEntry> {
        val (systemic, nonSystemic) = treatmentHistory.sortedWith(TreatmentHistoryAscendingDateComparator()).partition {
            it.treatments.any { treatment -> treatment.isSystemic }
        }

        val maxDate =
            (questionnaireDate ?: referenceDate(treatmentHistory, registrationDate)).plusMonths(BUFFER_MONTHS_AFTER_LAST_TREATMENT_DATE)

        val systemicWithStopDate = systemic.mapIndexed { index, current ->
            val details = current.treatmentHistoryDetails
            val startDateBeforeMaxStopDate = isBeforeDate(maxDate, current.startYear, current.startMonth) == true || isExactYearAndMonth(
                maxDate,
                current.startYear,
                current.startMonth
            )

            val updatedDetails = when {
                details?.stopYear != null && details.stopMonth == null -> {
                    val maxStopMonth = if (index < systemic.size - 1) {
                        val next = systemic[index + 1]
                        next.startMonth?.takeIf { next.startYear == details.stopYear } ?: LAST_MONTH
                    } else {
                        LAST_MONTH
                    }
                    details.copy(maxStopMonth = maxStopMonth)
                }

                details?.stopYear == null -> {
                    val (maxStopYear, maxStopMonth) = when {
                        index < systemic.size - 1 -> {
                            val next = systemic[index + 1]
                            next.startYear to next.startMonth
                        }

                        startDateBeforeMaxStopDate -> maxDate.year to maxDate.monthValue
                        else -> null to null
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

    private fun referenceDate(treatmentHistory: List<TreatmentHistoryEntry>, registrationDate: LocalDate): LocalDate {
        val maxDateAllTreatments = treatmentHistory.asSequence()
            .mapNotNull { treatment ->
                treatment.treatmentHistoryDetails?.stopYear?.let { stopYear ->
                    YearMonth.of(stopYear, treatment.treatmentHistoryDetails?.stopMonth ?: 1).atEndOfMonth()
                } ?: treatment.startYear?.let { startYear ->
                    YearMonth.of(startYear, treatment.startMonth ?: 1).atEndOfMonth()
                }
            }
            .maxOrNull()
        return maxDateAllTreatments?.let { maxOf(registrationDate, it) } ?: registrationDate
    }
}