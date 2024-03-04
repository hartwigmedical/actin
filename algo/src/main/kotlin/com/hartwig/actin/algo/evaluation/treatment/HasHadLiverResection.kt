package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.clinical.datamodel.treatment.TreatmentCategory

class HasHadLiverResection : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val lowercaseTreatmentNames = record.clinical.oncologicalHistory
            .flatMap { entry -> entry.treatments.flatMap { it.synonyms + it.name }.map(String::lowercase) }

        return when {
            lowercaseTreatmentNames.any { it.contains("liver") && it.contains(RESECTION_KEYWORD) } -> {
                EvaluationFactory.pass("Patient has had a liver resection", "Had had liver resection")
            }

            lowercaseTreatmentNames.any { it.contains(RESECTION_KEYWORD) } || record.clinical.oncologicalHistory.any { entry ->
                entry.treatments.any { it.categories().contains(TreatmentCategory.SURGERY) && it.name.isEmpty() }
            } -> {
                EvaluationFactory.undetermined(
                    "Could not be determined whether patient has had a liver resection",
                    "Liver resection undetermined"
                )
            }

            else -> {
                EvaluationFactory.fail("Patient has not had a liver resection", "Has not had liver resection")
            }
        }
    }

    companion object {
        const val RESECTION_KEYWORD = "resection"
    }
}