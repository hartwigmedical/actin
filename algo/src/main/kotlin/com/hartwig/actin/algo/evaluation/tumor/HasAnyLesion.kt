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
                return EvaluationFactory.undetermined("Undetermined if lesions are present (data missing)")
            }
        }

        return when {
            tumorDetails.hasConfirmedLesions() -> {
                EvaluationFactory.pass("Has at least one lesion")
            }

            tumorDetails.hasSuspectedLesions() -> {
                EvaluationFactory.warn("Lesions present but suspected lesions only")
            }

            else -> {
                EvaluationFactory.fail("No lesions present")
            }
        }
    }
}