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
                    EvaluationFactory.undetermined("Undetermined if active brain metastases based on provided lesions")
                }

                hasActiveBrainLesions == true -> EvaluationFactory.pass("Active brain metastases in provided lesions")

                else -> EvaluationFactory.fail("No known active brain metastases in provided lesions")
            }
        }
    }

    private fun undeterminedActivityEvaluation(prefix: String): Evaluation {
        return EvaluationFactory.undetermined("$prefix metastases in provided lesions but unknown if active (data missing)")
    }
}