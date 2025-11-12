package com.hartwig.actin.clinical.interpretation

import com.hartwig.actin.calendar.DateComparison
import com.hartwig.actin.datamodel.clinical.treatment.history.StopReason
import com.hartwig.actin.datamodel.clinical.treatment.history.TreatmentHistoryEntry
import com.hartwig.actin.datamodel.clinical.treatment.history.TreatmentResponse

private const val MIN_WEEKS_TO_ASSUME_STOP_DUE_TO_PD = 26 // half year

object ProgressiveDiseaseFunctions {

    fun treatmentResultedInPD(treatment: TreatmentHistoryEntry): Boolean? {
        val bestResponse = treatment.treatmentHistoryDetails?.bestResponse
        val stopReason = treatment.treatmentHistoryDetails?.stopReason
        val treatmentDuration = DateComparison.minWeeksBetweenDates(
            treatment.startYear,
            treatment.startMonth,
            treatment.stopYear(),
            treatment.stopMonth()
        )

        return when {
            bestResponse == TreatmentResponse.PROGRESSIVE_DISEASE || stopReason == StopReason.PROGRESSIVE_DISEASE -> true

            stopReason == null && treatmentDuration != null && treatmentDuration > MIN_WEEKS_TO_ASSUME_STOP_DUE_TO_PD -> true

            stopReason != null -> false

            else -> null
        }
    }
}