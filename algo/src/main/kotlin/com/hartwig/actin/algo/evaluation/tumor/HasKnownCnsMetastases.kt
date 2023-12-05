package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction

class HasKnownCnsMetastases : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val hasCnsLesions = record.clinical().tumor().hasCnsLesions()
        val hasBrainLesions = record.clinical().tumor().hasBrainLesions()
        if (hasCnsLesions == null && hasBrainLesions == null) {
            return EvaluationFactory.fail(
                "Data regarding presence of CNS metastases is missing, assuming there are none",
                "Assuming no known CNS metastases"
            )
        }
        var hasKnownCnsMetastases = hasCnsLesions != null && hasCnsLesions
        var hasAtLeastActiveBrainMetastases = false
        if (hasBrainLesions != null && hasBrainLesions) {
            hasKnownCnsMetastases = true
            hasAtLeastActiveBrainMetastases = true
        }
        return if (!hasKnownCnsMetastases) {
            EvaluationFactory.fail("No known CNS metastases present", "No known CNS metastases")
        } else {
            if (hasAtLeastActiveBrainMetastases) {
                EvaluationFactory.pass("CNS (Brain) metastases are present", "CNS (Brain) metastases")
            } else {
                EvaluationFactory.pass("CNS metastases are present", "CNS metastases")
            }
        }
    }
}