package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.util.DateComparison
import com.hartwig.actin.clinical.datamodel.treatment.history.StopReason
import com.hartwig.actin.clinical.datamodel.treatment.history.TreatmentHistoryEntry
import com.hartwig.actin.clinical.datamodel.treatment.history.TreatmentResponse

private const val MIN_WEEKS_TO_ASSUME_STOP_DUE_TO_PD = 26 // half year

object ProgressiveDiseaseFunctions {

    fun treatmentResultedInPD(treatment: TreatmentHistoryEntry): Boolean? {
        val bestResponse = treatment.treatmentHistoryDetails()?.bestResponse()
        val stopReason = treatment.treatmentHistoryDetails()?.stopReason()
        val treatmentDuration = DateComparison.minWeeksBetweenDates(
            treatment.startYear(),
            treatment.startMonth(),
            treatment.treatmentHistoryDetails()?.stopYear(),
            treatment.treatmentHistoryDetails()?.stopMonth()
        )

        return when {
            bestResponse == TreatmentResponse.PROGRESSIVE_DISEASE || stopReason == StopReason.PROGRESSIVE_DISEASE -> true

            stopReason == null && treatmentDuration != null && treatmentDuration > MIN_WEEKS_TO_ASSUME_STOP_DUE_TO_PD -> true

            stopReason != null -> false

            else -> null
        }
    }
}