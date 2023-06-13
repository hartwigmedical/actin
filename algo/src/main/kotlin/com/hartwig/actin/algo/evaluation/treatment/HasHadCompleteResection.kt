package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.clinical.datamodel.treatment.TreatmentCategory

class HasHadCompleteResection internal constructor() : EvaluationFunction {
    override fun evaluate(record: PatientRecord): Evaluation {
        var hasHadCompleteResection = false
        var hasHadPotentialCompleteResection = false
        for (treatment in record.clinical().priorTumorTreatments()) {
            if (treatment.name().equals(COMPLETE_RESECTION, ignoreCase = true)) {
                hasHadCompleteResection = true
            }
            if (treatment.name().lowercase().contains(RESECTION_KEYWORD.lowercase())) {
                hasHadPotentialCompleteResection = true
            }
            if (treatment.categories().contains(TreatmentCategory.SURGERY) && treatment.name().isEmpty()) {
                hasHadPotentialCompleteResection = true
            }
        }
        return when {
            hasHadCompleteResection -> {
                EvaluationFactory.pass("Patient has had a complete resection", "Had had complete resection")
            }

            hasHadPotentialCompleteResection -> {
                EvaluationFactory.undetermined(
                    "Could not be determined whether patient has had a complete resection",
                    "Complete resection undetermined"
                )
            }

            else -> {
                EvaluationFactory.fail("Patient has not had a complete resection", "Has not had complete resection")
            }
        }
    }

    companion object {
        const val COMPLETE_RESECTION = "complete resection"
        const val RESECTION_KEYWORD = "resection"
    }
}