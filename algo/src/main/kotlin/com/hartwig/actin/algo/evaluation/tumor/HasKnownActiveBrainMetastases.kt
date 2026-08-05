package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.EvaluationLabels
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation

class HasKnownActiveBrainMetastases(private val labels: EvaluationLabels.Tumor) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        with(record.tumor) {
            listOf(hasBrainLesions, hasActiveBrainLesions, hasSuspectedBrainLesions)

            val unknownIfActive = hasActiveBrainLesions == null

            return when {
                unknownIfActive && hasBrainLesions == true -> undeterminedActivityEvaluation("Brain")
                unknownIfActive && hasSuspectedBrainLesions == true -> undeterminedActivityEvaluation("Suspected brain")

                unknownIfActive && hasBrainLesions == null -> {
                    EvaluationFactory.undetermined(labels.hasKnownActiveBrainMetastasesUndeterminedMissing())
                }

                hasActiveBrainLesions == true -> EvaluationFactory.pass(labels.hasKnownActiveBrainMetastasesPass())

                else -> EvaluationFactory.fail(labels.hasKnownActiveBrainMetastasesFail())
            }
        }
    }

    private fun undeterminedActivityEvaluation(prefix: String): Evaluation {
        return EvaluationFactory.undetermined(labels.hasKnownActiveBrainMetastasesUndeterminedActivity(prefix))
    }
}