package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation

class HasAnyLesion : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val tumorDetails = record.tumor
        with(tumorDetails) {
            if (confirmedCategoricalLesionList().all { it == null } && otherLesions == null && !hasSuspectedLesions()) {
                return EvaluationFactory.undetermined("Data about lesions is missing")
            }
        }

        return when {
            tumorDetails.hasConfirmedLesions() -> {
                EvaluationFactory.pass("Patient has at least one lesion")
            }

            tumorDetails.hasSuspectedLesions() -> {
                val message = "Lesions present but suspected lesions only"
                EvaluationFactory.warn(message)
            }

            else -> {
                EvaluationFactory.fail("No lesions present")
            }
        }
    }
}