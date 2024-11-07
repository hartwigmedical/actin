package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation


class HasKnownActiveCnsMetastases : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val tumorDetails = record.tumor

        val (hasCnsMetastases, hasActiveCnsLesions, hasSuspectedCnsMetastases) = listOf(
            tumorDetails.hasCnsLesions,
            tumorDetails.hasActiveCnsLesions,
            tumorDetails.hasSuspectedCnsLesions
        )

        val (hasBrainMetastases, hasActiveBrainMetastases, hasSuspectedBrainMetastases) = listOf(
            tumorDetails.hasBrainLesions,
            tumorDetails.hasActiveBrainLesions,
            tumorDetails.hasSuspectedBrainLesions
        )

        val unknownIfActive = hasActiveCnsLesions == null && hasActiveBrainMetastases == null

        val undeterminedSpecificMessage =
            "CNS metastases in history but data regarding active CNS metastases is missing - assuming inactive"
        val undeterminedGeneralMessage = "CNS metastases present but unknown if active (data missing)"

        return when {
            unknownIfActive && (hasCnsMetastases == true || hasBrainMetastases == true) -> {
                EvaluationFactory.undetermined(undeterminedSpecificMessage, undeterminedGeneralMessage)
            }

            unknownIfActive && (hasSuspectedCnsMetastases == true || hasSuspectedBrainMetastases == true) -> {
                EvaluationFactory.undetermined("Suspected $undeterminedSpecificMessage, Suspected $undeterminedGeneralMessage")
            }

            unknownIfActive && (hasCnsMetastases == null && hasBrainMetastases == null) -> {
                EvaluationFactory.undetermined("Unknown if (active) CNS metastases present", "Unknown if (active) CNS metastases present")
            }

            hasActiveCnsLesions == true -> EvaluationFactory.pass("Active CNS metastases are present", "Active CNS metastases")

            hasActiveBrainMetastases == true -> {
                EvaluationFactory.pass(
                    "Active CNS (Brain) metastases are present",
                    "Active CNS (Brain) metastases"
                )
            }

            else -> EvaluationFactory.fail("No known active CNS metastases present", "No known active CNS metastases")
        }
    }
}