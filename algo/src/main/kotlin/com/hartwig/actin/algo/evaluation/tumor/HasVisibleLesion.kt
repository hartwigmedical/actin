package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation

class HasVisibleLesion : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        return when (record.tumor.hasMeasurableDisease) {
            true -> {
                EvaluationFactory.recoverablePass("Has visible lesion (because known measurable disease)")
            }
            else -> {
                EvaluationFactory.undetermined("Undetermined if patient may have visible lesion")
            }
        }
    }
}