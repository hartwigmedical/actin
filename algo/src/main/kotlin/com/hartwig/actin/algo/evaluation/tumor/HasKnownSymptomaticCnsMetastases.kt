package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation

class HasKnownSymptomaticCnsMetastases : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        with(record.tumor) {
            val unknownIfSymptomatic = hasSymptomaticCnsLesions == null && hasSymptomaticBrainLesions == null
            val undeterminedMessage = "CNS metastases in provided lesions but unknown if symptomatic (data missing)"

            return when {
                unknownIfSymptomatic && (hasCnsLesions == true || hasBrainLesions == true) -> {
                    EvaluationFactory.undetermined(undeterminedMessage)
                }

                unknownIfSymptomatic && (hasSuspectedCnsLesions == true || hasSuspectedBrainLesions == true) -> {
                    EvaluationFactory.undetermined("Suspected $undeterminedMessage")
                }

                unknownIfSymptomatic && (hasCnsLesions == null && hasBrainLesions == null) -> {
                    EvaluationFactory.undetermined("Undetermined if symptomatic CNS metastases based on provided lesions")
                }

                hasSymptomaticCnsLesions == true -> EvaluationFactory.pass("Symptomatic CNS metastases in provided lesions")

                hasSymptomaticBrainLesions == true -> EvaluationFactory.pass("Symptomatic CNS (Brain) metastases in provided lesions")

                else -> EvaluationFactory.fail("No known symptomatic CNS metastases in provided lesions")
            }
        }
    }
}