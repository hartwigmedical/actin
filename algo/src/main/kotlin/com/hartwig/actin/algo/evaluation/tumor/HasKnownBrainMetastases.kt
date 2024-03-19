package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction

class HasKnownBrainMetastases : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val hasBrainMetastases = record.tumor.hasBrainLesions
            ?: return EvaluationFactory.fail(
                "Data regarding presence of brain metastases is missing - assuming there are none",
                "Assuming no known brain metastases"
            )
        return if (hasBrainMetastases) {
            EvaluationFactory.pass("Brain metastases are present", "Brain metastases")
        } else {
            EvaluationFactory.fail("No known brain metastases present", "No known brain metastases")
        }
    }
}