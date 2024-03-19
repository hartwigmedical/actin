package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction

class HasEvaluableDisease : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {

        return when (record.tumor.hasMeasurableDisease) {
            true -> {
                EvaluationFactory.recoverablePass(
                    "Patient has measurable disease and hence will have evaluable disease",
                    "Has evaluable disease"
                )
            }
            false -> {
                EvaluationFactory.recoverableUndetermined(
                    "Patient has no measurable disease but unknown if patient may still have evaluable disease",
                    "Undetermined evaluable disease"
                )
            }
            else -> {
                EvaluationFactory.recoverableUndetermined(
                    "Undetermined if patient may have evaluable disease",
                    "Undetermined evaluable disease"
                )
            }
        }
    }
}