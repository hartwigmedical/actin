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
                    val message = "Unknown if active brain metastases present (data missing)"
                    EvaluationFactory.undetermined(message, message)
                }

                hasActiveBrainLesions == true -> EvaluationFactory.pass("Active brain metastases are present", "Active brain metastases")

                else -> EvaluationFactory.fail("No known active brain metastases present", "No known active brain metastases")
            }
        }
    }

    private fun undeterminedActivityEvaluation(prefix: String): Evaluation {
        return EvaluationFactory.undetermined(
            "$prefix metastases in history but data regarding active brain metastases is missing - assuming inactive",
            "$prefix metastases present but unknown if active (data missing)"
        )
    }
}