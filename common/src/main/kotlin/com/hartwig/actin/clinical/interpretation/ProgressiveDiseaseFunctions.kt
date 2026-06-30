package com.hartwig.actin.clinical.interpretation

import com.hartwig.actin.calendar.DateComparison
import com.hartwig.actin.datamodel.clinical.treatment.Treatment
import com.hartwig.actin.datamodel.clinical.treatment.history.StopReason
import com.hartwig.actin.datamodel.clinical.treatment.history.TreatmentHistoryEntry
import com.hartwig.actin.datamodel.clinical.treatment.history.TreatmentResponse
import java.time.YearMonth

object ProgressiveDiseaseFunctions {

    private const val MIN_WEEKS_TO_ASSUME_STOP_DUE_TO_PD = 26L // half year

    fun treatmentResultedInPD(treatment: TreatmentHistoryEntry, treatmentHistory: List<TreatmentHistoryEntry> = emptyList()): Boolean? {
        val bestResponse = treatment.treatmentHistoryDetails?.bestResponse
        return when {
            bestResponse == TreatmentResponse.PROGRESSIVE_DISEASE -> true
            else -> treatmentStoppedDueToPD(treatment, treatmentHistory)
        }
    }

    fun treatmentStoppedDueToPD(treatment: TreatmentHistoryEntry, treatmentHistory: List<TreatmentHistoryEntry> = emptyList()): Boolean? {
        val stopReason = treatment.treatmentHistoryDetails?.stopReason
        val treatmentDuration = DateComparison.minWeeksBetweenDates(
            treatment.startYear, treatment.startMonth, treatment.stopYear(), treatment.stopMonth()
        )
        return when {
            stopReason == StopReason.PROGRESSIVE_DISEASE -> true
            stopReason != null -> false
            hasSubsequentTreatmentLine(treatment, treatmentHistory) == true -> true
            treatmentDuration != null && treatmentDuration > MIN_WEEKS_TO_ASSUME_STOP_DUE_TO_PD -> true
            else -> null
        }
    }

    private fun hasSubsequentTreatmentLine(entry: TreatmentHistoryEntry, history: List<TreatmentHistoryEntry>): Boolean? {
        val stopYear = entry.stopYear()
        val stopMonth = entry.stopMonth()
        val entryStartYear = entry.startYear
        val others = history.filter { it !== entry && it.allTreatments().any(Treatment::isSystemic) }

        val hasSubsequent = if (stopYear == null) null else {
            val entryStop = YearMonth.of(stopYear, stopMonth ?: 12)
            others.mapNotNull { other -> other.startYear?.let { year -> other to year } }.any { (other, otherStartYear) ->
                val otherStart = YearMonth.of(otherStartYear, other.startMonth ?: 1)
                when {
                    otherStart.isAfter(entryStop) -> gapSuggestsPD(stopYear, stopMonth, otherStartYear, other.startMonth)
                    entryStartYear != null && otherStart.isAfter(YearMonth.of(entryStartYear, entry.startMonth ?: 12)) -> {
                        val otherStopYear = other.stopYear()
                        otherStopYear == null || YearMonth.of(otherStopYear, other.stopMonth() ?: 1).isAfter(entryStop)
                    }
                    else -> false
                }
            }
        }

        return when {
            hasSubsequent == true -> true
            hasSubsequent == null -> null
            others.any { it.startYear == null } -> null
            else -> false
        }
    }

    private fun gapSuggestsPD(stopYear: Int, stopMonth: Int?, nextStartYear: Int, nextStartMonth: Int?): Boolean {
        val minGapWeeks = DateComparison.minWeeksBetweenDates(stopYear, stopMonth, nextStartYear, nextStartMonth)
        return minGapWeeks != null && minGapWeeks < MIN_WEEKS_TO_ASSUME_STOP_DUE_TO_PD
    }
}
