package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction

class HasKnownActiveCnsMetastases : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val hasCnsMetastases = record.tumor.hasCnsLesions
        // If a patient's active CNS lesion status is unknown, set to false if patient is known to have no CNS metastases
        val hasActiveCnsLesions = record.tumor.hasActiveCnsLesions ?: if (hasCnsMetastases == false) false else null

        val hasBrainMetastases = record.tumor.hasBrainLesions
        // If a patient's active brain metastases status is unknown, set to false if patient is known to have no brain metastases
        val hasActiveBrainMetastases = record.tumor.hasActiveBrainLesions ?: if (hasBrainMetastases == false) false else null

        if (hasActiveCnsLesions == null && hasActiveBrainMetastases == null) {
            return if (hasCnsMetastases == true || hasBrainMetastases == true) {
                EvaluationFactory.undetermined(
                    "CNS metastases in history but data regarding active CNS metastases is missing - assuming there are none",
                    "CNS metastases present but unknown if active (data missing)"
                )
            } else {
                EvaluationFactory.recoverableUndetermined(
                    "Data regarding presence of active CNS metastases is missing",
                    "Missing active CNS metastases data"
                )
            }
        }
        return when {
            hasActiveCnsLesions == true ->
                EvaluationFactory.pass("Active CNS metastases are present", "Active CNS metastases")

            hasActiveBrainMetastases == true ->
                EvaluationFactory.pass(
                    "Active CNS (Brain) metastases are present",
                    "Active CNS (Brain) metastases"
                )

            else ->
                EvaluationFactory.fail("No known active CNS metastases present", "No known active CNS metastases")
        }
    }
}