package com.hartwig.actin.clinical.interpretation

import com.hartwig.actin.calendar.DateComparison
import com.hartwig.actin.datamodel.clinical.treatment.history.StopReason
import com.hartwig.actin.datamodel.clinical.treatment.history.TreatmentHistoryEntry
import com.hartwig.actin.datamodel.clinical.treatment.history.TreatmentResponse
import java.time.YearMonth

private const val MIN_WEEKS_TO_ASSUME_STOP_DUE_TO_PD = 26 // half year

object ProgressiveDiseaseFunctions {

    fun treatmentResultedInPD(treatment: TreatmentHistoryEntry, hasSubsequentLine: Boolean = false): Boolean? {
        val bestResponse = treatment.treatmentHistoryDetails?.bestResponse
        return when {
            bestResponse == TreatmentResponse.PROGRESSIVE_DISEASE -> true
            else -> treatmentStoppedDueToPD(treatment, hasSubsequentLine)
        }
    }

    fun treatmentStoppedDueToPD(treatment: TreatmentHistoryEntry, hasSubsequentLine: Boolean = false): Boolean? {
        val stopReason = treatment.treatmentHistoryDetails?.stopReason
        val treatmentDuration = DateComparison.minWeeksBetweenDates(
            treatment.startYear,
            treatment.startMonth,
            treatment.stopYear(),
            treatment.stopMonth()
        )

        return when {
            stopReason == StopReason.PROGRESSIVE_DISEASE -> true

            stopReason == null && hasSubsequentLine -> true

            stopReason == null && treatmentDuration != null && treatmentDuration > MIN_WEEKS_TO_ASSUME_STOP_DUE_TO_PD -> true

            stopReason != null -> false

            else -> null
        }
    }

    fun hasSubsequentTreatmentLine(entry: TreatmentHistoryEntry, history: List<TreatmentHistoryEntry>): Boolean {
        val stopYear = entry.stopYear() ?: return false
        val entryStop = YearMonth.of(stopYear, entry.stopMonth() ?: 12)
        return history.any { other ->
            if (other === entry || other.startYear == null) return@any false
            if (!YearMonth.of(other.startYear!!, other.startMonth ?: 1).isAfter(entryStop)) return@any false
            val minGapWeeks = DateComparison.minWeeksBetweenDates(stopYear, entry.stopMonth(), other.startYear!!, other.startMonth)
            minGapWeeks != null && minGapWeeks < MIN_WEEKS_TO_ASSUME_STOP_DUE_TO_PD
        }
    }
}