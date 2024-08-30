package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation

class HasKnownActiveBrainMetastases : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val hasBrainMetastases = record.tumor.hasBrainLesions
        // If a patient's active brain metastases status is unknown, set to false if patient is known to have no brain metastases
        val hasActiveBrainMetastases = record.tumor.hasActiveBrainLesions ?: if (hasBrainMetastases == false) false else null
            ?: return if (hasBrainMetastases == true) {
                EvaluationFactory.undetermined(
                    "Brain metastases in history but data regarding active brain metastases is missing - assuming there are none",
                    "Brain metastases present but unknown if active (data missing)"
                )
            } else {
                EvaluationFactory.recoverableUndetermined(
                    "Data regarding presence of active brain metastases is missing",
                    "Missing active brain metastases data"
                )
        }

        return if (hasActiveBrainMetastases) {
            EvaluationFactory.pass("Active brain metastases are present", "Active brain metastases")
        } else {
            EvaluationFactory.fail("No known active brain metastases present", "No active brain metastases")
        }
    }
}