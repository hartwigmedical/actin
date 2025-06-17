package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation

class HasAnyLesion: EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val tumor = record.tumor

        return when {
            tumor.hasConfirmedLesions() -> EvaluationFactory.pass("Has at least one lesion")

            tumor.hasSuspectedLesions() -> EvaluationFactory.warn("Has only suspected lesions - undetermined if has lesions")

            with(tumor) { (confirmedCategoricalLesionList().any { it == null } || otherLesions == null) } -> {
                EvaluationFactory.undetermined("Undetermined if lesions are present (some lesion data missing)")
            }

            else -> EvaluationFactory.fail("Has no lesions")
        }
    }
}