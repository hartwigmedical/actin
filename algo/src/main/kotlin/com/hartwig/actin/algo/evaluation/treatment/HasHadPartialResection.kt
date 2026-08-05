package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.EvaluationLabels
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation

const val PARTIAL_RESECTION = "partial resection"

class HasHadPartialResection(private val labels: EvaluationLabels.Treatment) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val lowercaseTreatmentNames = record.oncologicalHistory
            .flatMap { entry -> entry.treatments.flatMap { it.synonyms + it.name }.map(String::lowercase) }

        return when {
            lowercaseTreatmentNames.contains(PARTIAL_RESECTION) -> EvaluationFactory.pass(labels.hasHadPartialResectionPass())

            lowercaseTreatmentNames.any { name -> name == SURGERY || RESECTION_KEYWORDS.any(name::contains) } -> {
                EvaluationFactory.undetermined(labels.hasHadPartialResectionUndetermined())
            }

            else -> EvaluationFactory.fail(labels.hasHadPartialResectionFail())
        }
    }
}
