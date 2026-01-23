package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation

class HasKnownSymptomaticBrainMetastases : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        with(record.tumor) {
            val unknownIfSymptomatic = hasSymptomaticBrainLesions == null

            return when {
                unknownIfSymptomatic && hasBrainLesions == true -> {
                    EvaluationFactory.undetermined("Brain metastases present but unknown if symptomatic (data missing)")
                }

                unknownIfSymptomatic && hasBrainLesions == null -> {
                    EvaluationFactory.undetermined("Undetermined if symptomatic brain metastases present (data missing)")
                }

                hasSymptomaticBrainLesions == true -> EvaluationFactory.pass("Has symptomatic brain metastases")

                else -> EvaluationFactory.fail("No known symptomatic brain metastases present")
            }
        }
    }
}