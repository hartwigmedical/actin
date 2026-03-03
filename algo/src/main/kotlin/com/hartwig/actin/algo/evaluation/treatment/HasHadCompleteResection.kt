package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation

const val COMPLETE_RESECTION = "complete resection"

class HasHadCompleteResection : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val lowercaseTreatmentNames = record.oncologicalHistory
            .flatMap { entry -> entry.treatments.flatMap { it.synonyms + it.name }.map(String::lowercase) }

        return when {
            lowercaseTreatmentNames.contains(COMPLETE_RESECTION) -> EvaluationFactory.pass("Has had complete resection")

            lowercaseTreatmentNames.any { entry -> RESECTION_KEYWORDS.any { keyword -> entry.contains(keyword) } } ||
                    record.oncologicalHistory.any { entry -> entry.treatments.any { it.name.lowercase() == "surgery" } }
                -> EvaluationFactory.undetermined("Undetermined whether patient has had complete resection")

            else -> EvaluationFactory.fail("Has not had complete resection")
        }
    }

    companion object {
        val RESECTION_KEYWORDS = setOf("resection", "ectomy")
    }
}