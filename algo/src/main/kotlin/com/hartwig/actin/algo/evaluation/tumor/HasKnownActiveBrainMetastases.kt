package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation

class HasKnownActiveBrainMetastases : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val tumorDetails = record.tumor

        val (hasBrainMetastases, hasActiveBrainMetastases, hasSuspectedBrainMetastases) = listOf(
            tumorDetails.hasBrainLesions,
            tumorDetails.hasActiveBrainLesions,
            tumorDetails.hasSuspectedBrainLesions
        )

        val unknownIfActive = hasActiveBrainMetastases == null

        val undeterminedSpecificMessage =
            "metastases in history but data regarding active brain metastases is missing - assuming inactive"
        val undeterminedGeneralMessage = " metastases present but unknown if active (data missing)"

        return when {
            unknownIfActive && hasBrainMetastases == true -> {
                EvaluationFactory.undetermined("Brain $undeterminedSpecificMessage", "Brain $undeterminedGeneralMessage")
            }

            unknownIfActive && hasSuspectedBrainMetastases == true -> {
                EvaluationFactory.undetermined("Suspected brain $undeterminedSpecificMessage, Suspected brain $undeterminedGeneralMessage")
            }

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
}