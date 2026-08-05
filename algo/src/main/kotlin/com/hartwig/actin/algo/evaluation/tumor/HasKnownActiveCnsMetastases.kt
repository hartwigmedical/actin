package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.EvaluationLabels
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation

class HasKnownActiveCnsMetastases(private val labels: EvaluationLabels.Tumor) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        with(record.tumor) {
            val unknownIfActive = hasActiveCnsLesions == null && hasActiveBrainLesions == null

            return when {
                unknownIfActive && (hasCnsLesions == true || hasBrainLesions == true) -> {
                    EvaluationFactory.undetermined(labels.hasKnownActiveCnsMetastasesUndetermined())
                }

                unknownIfActive && (hasSuspectedCnsLesions == true || hasSuspectedBrainLesions == true) -> {
                    EvaluationFactory.undetermined(labels.hasKnownActiveCnsMetastasesUndeterminedSuspected())
                }

                unknownIfActive && (hasCnsLesions == null && hasBrainLesions == null) -> {
                    EvaluationFactory.undetermined(labels.hasKnownActiveCnsMetastasesUndeterminedMissing())
                }

                hasActiveCnsLesions == true -> EvaluationFactory.pass(labels.hasKnownActiveCnsMetastasesPass())

                hasActiveBrainLesions == true -> {
                    EvaluationFactory.pass(labels.hasKnownActiveCnsMetastasesPassBrain())
                }

                else -> EvaluationFactory.fail(labels.hasKnownActiveCnsMetastasesFail())
            }
        }
    }
}