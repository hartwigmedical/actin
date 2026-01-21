package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation

class HasKnownSymptomaticBrainMetastases : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        with(record.tumor) {
            listOf(hasSymptomaticBrainLesions, hasBrainLesions, hasActiveBrainLesions)

            val unknownIfSymptomatic = hasSymptomaticBrainLesions == null
            val undeterminedMessageEnding = "metastases present but unknown if symptomatic (data missing)"

            return when {
                unknownIfSymptomatic && hasBrainLesions == true -> EvaluationFactory.undetermined("Brain $undeterminedMessageEnding")

                unknownIfSymptomatic && hasBrainLesions == null -> {
                    EvaluationFactory.undetermined("Undetermined if symptomatic brain metastases present (data missing)")
                }

                hasBrainLesions == true && hasSymptomaticBrainLesions == true -> EvaluationFactory.pass("Has symptomatic brain metastases")

                else -> EvaluationFactory.fail("No known symptomatic brain metastases present")
            }
        }
    }
}