package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.EvaluationLabels
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation

class HasKnownSymptomaticCnsMetastases(private val labels: EvaluationLabels.Tumor) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        with(record.tumor) {
            val unknownIfSymptomatic = hasSymptomaticCnsLesions == null && hasSymptomaticBrainLesions == null

            return when {
                unknownIfSymptomatic && (hasCnsLesions == true || hasBrainLesions == true) -> {
                    EvaluationFactory.undetermined(labels.hasKnownSymptomaticCnsMetastasesUndetermined())
                }

                unknownIfSymptomatic && (hasSuspectedCnsLesions == true || hasSuspectedBrainLesions == true) -> {
                    EvaluationFactory.undetermined(labels.hasKnownSymptomaticCnsMetastasesUndeterminedSuspected())
                }

                unknownIfSymptomatic && (hasCnsLesions == null && hasBrainLesions == null) -> {
                    EvaluationFactory.undetermined(labels.hasKnownSymptomaticCnsMetastasesUndeterminedMissing())
                }

                hasSymptomaticCnsLesions == true -> EvaluationFactory.pass(labels.hasKnownSymptomaticCnsMetastasesPass())

                hasSymptomaticBrainLesions == true -> EvaluationFactory.pass(labels.hasKnownSymptomaticCnsMetastasesPassBrain())

                else -> EvaluationFactory.fail(labels.hasKnownSymptomaticCnsMetastasesFail())
            }
        }
    }
}