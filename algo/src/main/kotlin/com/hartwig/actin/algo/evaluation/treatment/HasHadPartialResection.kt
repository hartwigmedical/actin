package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.clinical.datamodel.treatment.TreatmentCategory

class HasHadPartialResection : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val lowercaseTreatmentNames = record.clinical.oncologicalHistory
            .flatMap { entry -> entry.treatments.flatMap { it.synonyms + it.name }.map(String::lowercase) }

        return when {
            lowercaseTreatmentNames.contains(PARTIAL_RESECTION) -> {
                EvaluationFactory.pass("Patient has had a partial resection", "Has had partial resection")
            }

            lowercaseTreatmentNames.any { it.contains(RESECTION_KEYWORD) } || record.clinical.oncologicalHistory.any { entry ->
                entry.treatments.any { it.categories().contains(TreatmentCategory.SURGERY) && it.name.isEmpty() }
            } -> {
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