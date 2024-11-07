package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation

class HasKnownBrainMetastases : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val tumorDetails = record.tumor
        val (hasBrainLesions, hasSuspectedBrainLesions) = listOf(tumorDetails.hasBrainLesions, tumorDetails.hasSuspectedBrainLesions)
        val undeterminedMessage = "Undetermined if brain metastases present"

        return when {
            hasBrainLesions == true -> {
                EvaluationFactory.pass("Brain metastases are present", "Brain metastases")
            }

            hasSuspectedBrainLesions == true -> {
                val message = "$undeterminedMessage (suspected lesions only)"
                EvaluationFactory.undetermined(message, message)
            }

            hasBrainLesions == null -> {
                val message = "$undeterminedMessage (data missing)"
                EvaluationFactory.recoverableUndetermined(message, message)
            }

            else -> EvaluationFactory.fail("No known brain metastases present", "No known brain metastases")
        }
    }
}