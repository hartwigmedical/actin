package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.EvaluationLabels
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation

class HasKnownBrainMetastases(private val labels: EvaluationLabels.Tumor) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        with(record.tumor) {
            return when {
                hasBrainLesions == true -> {
                    EvaluationFactory.pass(labels.hasKnownBrainMetastasesPass())
                }

                hasSuspectedBrainLesions == true -> {
                    val message = labels.hasKnownBrainMetastasesWarn()
                    EvaluationFactory.warn(message)
                }

                hasBrainLesions == null -> {
                    val message = labels.hasKnownBrainMetastasesUndetermined()
                    EvaluationFactory.undetermined(message)
                }

                else -> EvaluationFactory.fail(labels.hasKnownBrainMetastasesFail())
            }
        }
    }
}