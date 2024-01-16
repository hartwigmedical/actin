package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction

class HasUnresectablePeritonealMetastases : EvaluationFunction {
    override fun evaluate(record: PatientRecord): Evaluation {
        var hasPeritonealMetastases = false
        if (!record.clinical().tumor().otherLesions().isNullOrEmpty()) {
            for (lesion in record.clinical().tumor().otherLesions()!!) {
                if (lesion.contains("peritoneal", true) || lesion.contains("peritoneum", true)) {
                    hasPeritonealMetastases = true
                }
            }
        }

        return if (hasPeritonealMetastases) {
            EvaluationFactory.warn(
                "Undetermined if peritoneal metastases are unresectable",
                "Undetermined if peritoneal metastases are unresectable"
            )
        } else {
            EvaluationFactory.fail("Patient has no unresectable peritoneal metastases", "No unresectable peritoneal metastases")
        }
    }
}