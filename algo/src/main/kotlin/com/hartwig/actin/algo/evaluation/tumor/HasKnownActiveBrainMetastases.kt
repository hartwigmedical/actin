package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation

class HasKnownActiveBrainMetastases : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        with(record.tumor) {
            listOf(hasBrainLesions, hasActiveBrainLesions, hasSuspectedBrainLesions)

            val unknownIfActive = hasActiveBrainLesions == null

            return when {
                unknownIfActive && hasBrainLesions == true -> undeterminedActivityEvaluation("Brain")
                unknownIfActive && hasSuspectedBrainLesions == true -> undeterminedActivityEvaluation("Suspected brain")

                unknownIfActive && hasBrainLesions == null -> {
                    EvaluationFactory.undetermined("Undetermined if active brain metastases present (data missing)")
                }

                hasActiveBrainLesions == true -> EvaluationFactory.pass("Has active brain metastases")

                else -> EvaluationFactory.fail("No known active brain metastases present")
            }
        }
    }

    private fun undeterminedActivityEvaluation(prefix: String): Evaluation {
        return EvaluationFactory.undetermined("$prefix metastases present but unknown if active (data missing)")
    }
}