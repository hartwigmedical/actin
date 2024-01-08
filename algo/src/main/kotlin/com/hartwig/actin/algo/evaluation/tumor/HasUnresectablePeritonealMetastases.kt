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
                "Peritoneal metastases are present which could be unresectable",
                "Peritoneal metastases which could be unresectable"
            )
        } else {
            EvaluationFactory.fail("No peritoneal metastases present", "No peritoneal metastases")
        }
    }
}