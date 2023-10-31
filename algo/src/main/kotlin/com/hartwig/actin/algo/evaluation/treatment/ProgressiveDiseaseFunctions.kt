package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.clinical.datamodel.treatment.history.StopReason
import com.hartwig.actin.clinical.datamodel.treatment.history.TreatmentHistoryEntry
import com.hartwig.actin.clinical.datamodel.treatment.history.TreatmentResponse

object ProgressiveDiseaseFunctions {

    fun treatmentResultedInPDOption(treatment: TreatmentHistoryEntry): Boolean? {
        val bestResponse = treatment.treatmentHistoryDetails()?.bestResponse()
        val stopReason = treatment.treatmentHistoryDetails()?.stopReason()
        return when {
            bestResponse == TreatmentResponse.PROGRESSIVE_DISEASE || stopReason == StopReason.PROGRESSIVE_DISEASE -> true

            bestResponse != null && stopReason != null -> false

            else -> null
        }
    }
}