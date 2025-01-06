package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation

class HasKnownActiveCnsMetastases : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        with(record.tumor) {
            val unknownIfActive = hasActiveCnsLesions == null && hasActiveBrainLesions == null
            val undeterminedMessage = "CNS metastases present but unknown if active (data missing)"

            return when {
                unknownIfActive && (hasCnsLesions == true || hasBrainLesions == true) -> {
                    EvaluationFactory.undetermined(undeterminedMessage)
                }

                unknownIfActive && (hasSuspectedCnsLesions == true || hasSuspectedBrainLesions == true) -> {
                    EvaluationFactory.undetermined("Suspected $undeterminedMessage")
                }

                unknownIfActive && (hasCnsLesions == null && hasBrainLesions == null) -> {
                    EvaluationFactory.undetermined("Undetermined if (active) CNS metastases present (data missing)")
                }

                hasActiveCnsLesions == true -> EvaluationFactory.pass("Active CNS metastases present")

                hasActiveBrainLesions == true -> {
                    EvaluationFactory.pass("Active CNS (Brain) metastases present")
                }

                else -> EvaluationFactory.fail("No known active CNS metastases present")
            }
        }
    }
}