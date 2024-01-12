package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction

class HasKnownActiveBrainMetastases : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val hasBrainMetastases = record.clinical.tumor.hasBrainLesions
        // If a patient's active brain metastases status is unknown, set to false if patient is known to have no brain metastases
        val hasActiveBrainMetastases = record.clinical.tumor.hasActiveBrainLesions ?: if (hasBrainMetastases == false) false else {
            return EvaluationFactory.undetermined(
                "Data regarding presence of active brain metastases is missing", "Missing active brain metastases data"
            )
        }

        return if (hasActiveBrainMetastases) {
            EvaluationFactory.pass("Active brain metastases are present", "Active brain metastases")
        } else {
            EvaluationFactory.fail("No known active brain metastases present", "No active brain metastases")
        }
    }
}