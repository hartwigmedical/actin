package com.hartwig.actin.clinical.feed.treatment

import com.hartwig.actin.calendar.DateComparison.isBeforeDate
import com.hartwig.actin.calendar.DateComparison.isExactYearAndMonth
import com.hartwig.actin.clinical.sort.TreatmentHistoryAscendingDateComparator
import com.hartwig.actin.datamodel.clinical.treatment.history.TreatmentHistoryDetails
import com.hartwig.actin.datamodel.clinical.treatment.history.TreatmentHistoryEntry
import java.time.LocalDate
import java.time.YearMonth

object TreatmentHistoryEntryFunctions {

    private const val DECEMBER = 12
    private const val ONE_MONTH: Long = 1

    fun setMaxStopDate(
        treatmentHistory: List<TreatmentHistoryEntry>,
        questionnaireDate: LocalDate?,
        registrationDate: LocalDate
    ): List<TreatmentHistoryEntry> {
        val (systemic, nonSystemic) = treatmentHistory.sortedWith(TreatmentHistoryAscendingDateComparator()).partition {
            it.treatments.any { treatment -> treatment.isSystemic }
        }

        val maxDate = maxDate(treatmentHistory, questionnaireDate, registrationDate)

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
                        if (next.startYear == details.stopYear) next.startMonth ?: DECEMBER else DECEMBER
                    } else {
                        DECEMBER
                    }
                    details.copy(maxStopMonth = maxStopMonth)
                }

                details?.stopYear == null -> {
                    val (maxStopYear, maxStopMonth) = if (index < systemic.size - 1) {
                        val next = systemic[index + 1]
                        next.startYear to next.startMonth
                    } else if (startDateBeforeMaxStopDate) maxDate.year to maxDate.monthValue else null to null

                    details?.copy(maxStopYear = maxStopYear, maxStopMonth = maxStopMonth)
                        ?: TreatmentHistoryDetails(maxStopYear = maxStopYear, maxStopMonth = maxStopMonth)
                }

                else -> details
            }

            current.copy(treatmentHistoryDetails = updatedDetails)
        }

        return systemicWithStopDate + nonSystemic
    }

    private fun maxDate(
        treatmentHistory: List<TreatmentHistoryEntry>,
        questionnaireDate: LocalDate?,
        registrationDate: LocalDate
    ): LocalDate {
        val maxDateAllTreatments = treatmentHistory.mapNotNull { treatment ->
            treatment.startYear?.let {
                YearMonth.of(
                    treatment.startYear!!,
                    treatment.startMonth ?: 1
                ).atEndOfMonth()
            }
        }.maxOrNull()
        val referenceDate = questionnaireDate ?: maxDateAllTreatments?.let { maxOf(registrationDate, it) } ?: registrationDate
        return referenceDate.plusMonths(ONE_MONTH)
    }
}