package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation

class HasEvaluableDisease : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        return when (record.tumor.hasMeasurableDisease) {
            true -> {
                EvaluationFactory.recoverablePass("Has evaluable disease (because known measurable disease)")
            }
            else -> {
                EvaluationFactory.recoverableUndetermined("Undetermined if patient may have evaluable disease")
            }
        }
    }
}