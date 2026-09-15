package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation

class HasAnyLesion: EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val tumor = record.tumor

        return when {
            tumor.hasConfirmedLesions() -> EvaluationFactory.pass("At least one lesion in provided lesions")

            tumor.hasSuspectedLesions() -> EvaluationFactory.warn(
                "Undetermined if lesions are present based on provided lesions (only suspected lesions)"
            )

            with(tumor) { (confirmedCategoricalLesionList().any { it == null } || otherLesions == null) } -> {
                EvaluationFactory.undetermined("Undetermined presence of lesions based on provided lesions")
            }

            else -> EvaluationFactory.fail("No lesions provided")
        }
    }
}