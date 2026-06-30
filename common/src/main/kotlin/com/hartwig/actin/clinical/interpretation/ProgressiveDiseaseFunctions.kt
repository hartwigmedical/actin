package com.hartwig.actin.clinical.interpretation

import com.hartwig.actin.calendar.DateComparison
import com.hartwig.actin.datamodel.clinical.treatment.history.StopReason
import com.hartwig.actin.datamodel.clinical.treatment.history.TreatmentHistoryEntry
import com.hartwig.actin.datamodel.clinical.treatment.history.TreatmentResponse

const val MIN_WEEKS_TO_ASSUME_STOP_DUE_TO_PD = 26L // half year

object ProgressiveDiseaseFunctions {

    fun treatmentResultedInPD(treatment: TreatmentHistoryEntry, hasSubsequentLine: Boolean? = null): Boolean? {
        val bestResponse = treatment.treatmentHistoryDetails?.bestResponse
        return when {
            bestResponse == TreatmentResponse.PROGRESSIVE_DISEASE -> true
            else -> treatmentStoppedDueToPD(treatment, hasSubsequentLine)
        }
    }

    fun treatmentStoppedDueToPD(treatment: TreatmentHistoryEntry, hasSubsequentLine: Boolean? = null): Boolean? {
        val stopReason = treatment.treatmentHistoryDetails?.stopReason
        val treatmentDuration = DateComparison.minWeeksBetweenDates(
            treatment.startYear,
            treatment.startMonth,
            treatment.stopYear(),
            treatment.stopMonth()
        )

        return when {
            stopReason == StopReason.PROGRESSIVE_DISEASE -> true
            stopReason != null -> false
            hasSubsequentLine == true -> true
            treatmentDuration != null && treatmentDuration > MIN_WEEKS_TO_ASSUME_STOP_DUE_TO_PD -> true
            else -> null
        }
    }

    fun isWithinPDInferenceGap(stopYear: Int, stopMonth: Int?, nextStartYear: Int, nextStartMonth: Int?): Boolean {
        val minGapWeeks = DateComparison.minWeeksBetweenDates(stopYear, stopMonth, nextStartYear, nextStartMonth)
        return minGapWeeks != null && minGapWeeks < MIN_WEEKS_TO_ASSUME_STOP_DUE_TO_PD
    }
}