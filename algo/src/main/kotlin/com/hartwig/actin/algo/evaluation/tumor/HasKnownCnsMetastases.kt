package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation

class HasKnownCnsMetastases : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val hasCnsLesions = record.tumor.hasCnsLesions()
        val hasBrainLesions = record.tumor.hasBrainLesions()
        if (hasCnsLesions == null && hasBrainLesions == null) {
            return EvaluationFactory.fail(
                "Data regarding presence of CNS metastases is missing - assuming there are none",
                "Assuming no known CNS metastases"
            )
        }
        val (hasKnownCnsMetastases, hasAtLeastActiveBrainMetastases) = if (hasBrainLesions == true) {
            Pair(true, true)
        } else {
            Pair(hasCnsLesions != null && hasCnsLesions, false)
        }
        return if (!hasKnownCnsMetastases) {
            EvaluationFactory.fail("No known CNS metastases present", "No known CNS metastases")
        } else if (hasAtLeastActiveBrainMetastases) {
            EvaluationFactory.pass("CNS (Brain) metastases are present", "CNS (Brain) metastases")
        } else {
            EvaluationFactory.pass("CNS metastases are present", "CNS metastases")
        }
    }
}