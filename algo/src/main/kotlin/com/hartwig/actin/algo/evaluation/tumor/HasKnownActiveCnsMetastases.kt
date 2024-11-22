package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation

class HasKnownActiveCnsMetastases : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        with(record.tumor) {
            val unknownIfActive = hasActiveCnsLesions == null && hasActiveBrainLesions == null
            val undeterminedSpecificMessage =
                "CNS metastases in history but data regarding active CNS metastases is missing - assuming inactive"
            val undeterminedGeneralMessage = "CNS metastases present but unknown if active (data missing)"

            return when {
                unknownIfActive && (hasCnsLesions == true || hasBrainLesions == true) -> {
                    EvaluationFactory.undetermined(undeterminedSpecificMessage, undeterminedGeneralMessage)
                }

                unknownIfActive && (hasSuspectedCnsLesions == true || hasSuspectedBrainLesions == true) -> {
                    EvaluationFactory.undetermined("Suspected $undeterminedSpecificMessage, Suspected $undeterminedGeneralMessage")
                }

                unknownIfActive && (hasCnsLesions == null && hasBrainLesions == null) -> {
                    val message = "Unknown if (active) CNS metastases present (data missing)"
                    EvaluationFactory.undetermined(message, message)
                }

                hasActiveCnsLesions == true -> EvaluationFactory.pass("Active CNS metastases are present", "Active CNS metastases")

                hasActiveBrainLesions == true -> {
                    EvaluationFactory.pass(
                        "Active CNS (Brain) metastases are present",
                        "Active CNS (Brain) metastases"
                    )
                }

                else -> EvaluationFactory.fail("No known active CNS metastases present", "No known active CNS metastases")
            }
        }
    }
}