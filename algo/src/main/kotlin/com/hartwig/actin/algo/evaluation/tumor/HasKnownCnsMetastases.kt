package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.EvaluationLabels
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation

class HasKnownCnsMetastases(private val labels: EvaluationLabels.Tumor) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        with(record.tumor) {

            return when {
                hasCnsLesions == true -> {
                    EvaluationFactory.pass(labels.hasKnownCnsMetastasesPass())
                }

                hasBrainLesions == true -> {
                    EvaluationFactory.pass(labels.hasKnownCnsMetastasesPassBrain())
                }

                hasSuspectedCnsLesions == true || hasSuspectedBrainLesions == true -> {
                    val message = labels.hasKnownCnsMetastasesWarn()
                    EvaluationFactory.warn(message)
                }

                hasCnsLesions == null || hasBrainLesions == null -> {
                    val message = labels.hasKnownCnsMetastasesUndetermined()
                    EvaluationFactory.undetermined(message)
                }

                else -> EvaluationFactory.fail(labels.hasKnownCnsMetastasesFail())
            }
        }
    }
}