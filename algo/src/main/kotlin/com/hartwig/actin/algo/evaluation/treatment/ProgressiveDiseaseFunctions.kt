package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.clinical.datamodel.treatment.PriorTumorTreatment
import com.hartwig.actin.clinical.datamodel.treatment.history.StopReason
import com.hartwig.actin.clinical.datamodel.treatment.history.TreatmentHistoryEntry
import com.hartwig.actin.clinical.datamodel.treatment.history.TreatmentResponse

internal object ProgressiveDiseaseFunctions {
    const val PD_LABEL = "PD"

    fun treatmentResultedInPDOption(treatment: TreatmentHistoryEntry): Boolean? {
        val bestResponse = treatment.therapyHistoryDetails()?.bestResponse()
        val stopReason = treatment.therapyHistoryDetails()?.stopReason()
        return when {
            bestResponse == TreatmentResponse.PROGRESSIVE_DISEASE || stopReason == StopReason.PROGRESSIVE_DISEASE -> true

            bestResponse != null && stopReason != null -> false

            else -> null
        }
    }

    fun treatmentResultedInPDOption(treatment: PriorTumorTreatment): Boolean? {
        return when {
            PD_LABEL.equals(treatment.stopReason(), ignoreCase = true) || PD_LABEL.equals(treatment.bestResponse(), ignoreCase = true) ->
                true

            treatment.stopReason() != null && treatment.bestResponse() != null -> false

            else -> null
        }
    }
}