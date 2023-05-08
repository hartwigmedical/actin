package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.clinical.datamodel.PriorTumorTreatment

internal object ProgressiveDiseaseFunctions {

    const val PD_LABEL = "PD"
    fun treatmentResultedInPDOption(treatment: PriorTumorTreatment): Boolean? {
        return if (PD_LABEL.equals(treatment.stopReason(), ignoreCase = true) || PD_LABEL.equals(
                treatment.bestResponse(),
                ignoreCase = true
            )
        ) {
            true
        } else if (treatment.stopReason() != null && treatment.bestResponse() != null) {
            false
        } else {
            null
        }
    }
}