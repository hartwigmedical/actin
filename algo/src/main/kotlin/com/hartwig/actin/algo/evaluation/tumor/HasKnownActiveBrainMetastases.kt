package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation

class HasKnownActiveBrainMetastases : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val (hasBrainMetastases, hasActiveBrainMetastases, hasSuspectedBrainMetastases) =
            with(record.tumor) {
                listOf(hasBrainLesions, hasActiveBrainLesions, hasSuspectedBrainLesions)
            }

        val unknownIfActive = hasActiveBrainMetastases == null

        return when {
            unknownIfActive && hasBrainMetastases == true -> undeterminedActivityEvaluation("Brain")
            unknownIfActive && hasSuspectedBrainMetastases == true -> undeterminedActivityEvaluation("Suspected brain")

            unknownIfActive && hasBrainMetastases == null -> {
                EvaluationFactory.recoverableUndetermined(
                    "Unknown if (active) brain metastases present",
                    "Unknown if (active) brain metastases present"
                )
            }

            hasActiveBrainMetastases == true -> EvaluationFactory.pass("Active brain metastases are present", "Active brain metastases")

            else -> EvaluationFactory.fail("No known active brain metastases present", "No known active brain metastases")
        }
    }

    private fun undeterminedActivityEvaluation(prefix: String): Evaluation {
        return EvaluationFactory.undetermined(
            "$prefix metastases in history but data regarding active brain metastases is missing - assuming inactive",
            "$prefix metastases present but unknown if active (data missing)"
        )
    }
}