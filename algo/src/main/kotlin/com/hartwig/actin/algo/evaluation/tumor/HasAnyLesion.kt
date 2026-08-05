package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.EvaluationLabels
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation

class HasAnyLesion(private val labels: EvaluationLabels.Tumor) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val tumor = record.tumor

        return when {
            tumor.hasConfirmedLesions() -> EvaluationFactory.pass(labels.hasAnyLesionPass())

            tumor.hasSuspectedLesions() -> EvaluationFactory.warn(labels.hasAnyLesionWarn())

            with(tumor) { (confirmedCategoricalLesionList().any { it == null } || otherLesions == null) } -> {
                EvaluationFactory.undetermined(labels.hasAnyLesionUndetermined())
            }

            else -> EvaluationFactory.fail(labels.hasAnyLesionFail())
        }
    }
}