package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation

class HasKnownCnsMetastases : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val tumorDetails = record.tumor
        val (hasCnsLesions, hasSuspectedCnsLesions) = listOf(tumorDetails.hasCnsLesions, tumorDetails.hasSuspectedCnsLesions)
        val (hasBrainLesions, hasSuspectedBrainLesions) = listOf(tumorDetails.hasBrainLesions, tumorDetails.hasSuspectedBrainLesions)
        val undeterminedMessage = "Undetermined if CNS metastases present"

        return when {
            hasCnsLesions == true -> {
                EvaluationFactory.pass("CNS metastases are present", "CNS metastases")
            }

            hasBrainLesions == true -> {
                EvaluationFactory.pass("Brain metastases are present", "Brain metastases")
            }

            hasSuspectedCnsLesions == true || hasSuspectedBrainLesions == true -> {
                val message = "$undeterminedMessage (suspected lesions only)"
                EvaluationFactory.undetermined(message, message)
            }

            hasCnsLesions == null || hasBrainLesions == null -> {
                val message = "$undeterminedMessage (data missing)"
                EvaluationFactory.recoverableUndetermined(message, message)
            }

            else -> EvaluationFactory.fail("No known CNS metastases present", "No known CNS metastases")
        }
    }
}