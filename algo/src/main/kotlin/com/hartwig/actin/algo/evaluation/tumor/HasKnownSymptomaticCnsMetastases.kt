package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation

class HasKnownSymptomaticCnsMetastases : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        with(record.tumor) {
            val unknownIfSymptomatic = hasSymptomaticCnsLesions == null && hasSymptomaticBrainLesions == null
            val undeterminedMessage = "CNS metastases present but unknown if symptomatic (data missing)"

            return when {
                unknownIfSymptomatic && (hasCnsLesions == true || hasBrainLesions == true) -> {
                    EvaluationFactory.undetermined(undeterminedMessage)
                }

                unknownIfSymptomatic && (hasSuspectedCnsLesions == true || hasSuspectedBrainLesions == true) -> {
                    EvaluationFactory.undetermined("Suspected $undeterminedMessage")
                }

                unknownIfSymptomatic && (hasCnsLesions == null && hasBrainLesions == null) -> {
                    EvaluationFactory.undetermined("Undetermined if symptomatic CNS metastases present (data missing)")
                }

                hasSymptomaticCnsLesions == true -> EvaluationFactory.pass("Has symptomatic CNS metastases")

                hasSymptomaticBrainLesions == true -> EvaluationFactory.pass("Has symptomatic CNS (Brain) metastases")

                else -> EvaluationFactory.fail("No known symptomatic CNS metastases present")
            }
        }
    }
}