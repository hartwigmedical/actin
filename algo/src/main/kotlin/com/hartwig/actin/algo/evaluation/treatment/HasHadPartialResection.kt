package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.clinical.datamodel.treatment.TreatmentCategory

class HasHadPartialResection internal constructor() : EvaluationFunction {
    override fun evaluate(record: PatientRecord): Evaluation {
        var hasHadPartialResection = false
        var hasHadPotentialPartialResection = false
        for (treatment in record.clinical().priorTumorTreatments()) {
            if (treatment.name().equals(PARTIAL_RESECTION, ignoreCase = true)) {
                hasHadPartialResection = true
            }
            if (treatment.name().lowercase().contains(RESECTION_KEYWORD.lowercase())) {
                hasHadPotentialPartialResection = true
            }
            if (treatment.categories().contains(TreatmentCategory.SURGERY) && treatment.name().isEmpty()) {
                hasHadPotentialPartialResection = true
            }
        }
        return when {
            hasHadPartialResection -> {
                EvaluationFactory.pass("Patient has had a partial resection", "Has had partial resection")
            }

            hasHadPotentialPartialResection -> {
                EvaluationFactory.undetermined(
                    "Could not be determined whether patient has had a partial resection",
                    "Partial resection undetermined"
                )
            }

            else -> {
                EvaluationFactory.fail("Patient has not had a partial resection", "Has not had partial resection")
            }
        }
    }

    companion object {
        const val PARTIAL_RESECTION = "partial resection"
        const val RESECTION_KEYWORD = "resection"
    }
}