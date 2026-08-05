package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.EvaluationLabels
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation

class HasKnownSymptomaticBrainMetastases(private val labels: EvaluationLabels.Tumor) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        with(record.tumor) {
            val unknownIfSymptomatic = hasSymptomaticBrainLesions == null

            return when {
                unknownIfSymptomatic && hasBrainLesions == true -> {
                    EvaluationFactory.undetermined(labels.hasKnownSymptomaticBrainMetastasesUndetermined())
                }

                unknownIfSymptomatic && hasBrainLesions == null -> {
                    EvaluationFactory.undetermined(labels.hasKnownSymptomaticBrainMetastasesUndeterminedMissing())
                }

                hasSymptomaticBrainLesions == true -> EvaluationFactory.pass(labels.hasKnownSymptomaticBrainMetastasesPass())

                else -> EvaluationFactory.fail(labels.hasKnownSymptomaticBrainMetastasesFail())
            }
        }
    }
}