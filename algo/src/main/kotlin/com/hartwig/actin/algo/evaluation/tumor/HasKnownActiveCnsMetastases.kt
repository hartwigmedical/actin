package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction

class HasKnownActiveCnsMetastases : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val hasCnsMetastases = record.clinical().tumor().hasCnsLesions()
        var hasActiveCnsLesions = record.clinical().tumor().hasActiveCnsLesions()
        val hasBrainMetastases = record.clinical().tumor().hasBrainLesions()
        var hasActiveBrainMetastases = record.clinical().tumor().hasActiveBrainLesions()

        // If a patient is known to have no cns metastases, update active to false in case it is unknown.
        if (hasCnsMetastases != null && !hasCnsMetastases) {
            hasActiveCnsLesions = hasActiveCnsLesions ?: false
        }

        // If a patient is known to have no brain metastases, update active to false in case it is unknown.
        if (hasBrainMetastases != null && !hasBrainMetastases) {
            hasActiveBrainMetastases = hasActiveBrainMetastases ?: false
        }
        if (hasActiveCnsLesions == null && hasActiveBrainMetastases == null) {
            return EvaluationFactory.undetermined(
                "Data regarding presence of active CNS metastases is missing",
                "Missing active CNS metastases data"
            )
        }
        return when {
            hasActiveCnsLesions == true ->
                EvaluationFactory.pass("Active CNS metastases are present", "Active CNS metastases")

            hasActiveBrainMetastases == true ->
                EvaluationFactory.pass(
                    "Active brain metastases are present, these are considered CNS metastases",
                    "Active brain (CNS) metastases"
                )

            else ->
                EvaluationFactory.fail("No known active CNS metastases present", "No known active CNS metastases")
        }
    }
}