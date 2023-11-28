package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.util.DateComparison
import com.hartwig.actin.clinical.datamodel.treatment.history.StopReason
import com.hartwig.actin.clinical.datamodel.treatment.history.TreatmentHistoryEntry
import com.hartwig.actin.clinical.datamodel.treatment.history.TreatmentResponse

object ProgressiveDiseaseFunctions {

    fun treatmentResultedInPDOption(treatment: TreatmentHistoryEntry): Boolean? {
        val bestResponse = treatment.treatmentHistoryDetails()?.bestResponse()
        val stopReason = treatment.treatmentHistoryDetails()?.stopReason()
        val treatmentDuration: Long? = DateComparison.minWeeksBetweenDates(
            treatment.startYear(),
            treatment.startMonth(),
            treatment.treatmentHistoryDetails()?.stopYear(),
            treatment.treatmentHistoryDetails()?.stopMonth()
        )

        return when {
            bestResponse == TreatmentResponse.PROGRESSIVE_DISEASE || stopReason == StopReason.PROGRESSIVE_DISEASE -> true

            stopReason == null && treatmentDuration != null && treatmentDuration > TreatmentConstants.minWeeksToAssumeStopDueToPD -> true

            stopReason != null -> false

            else -> null
        }
    }
}