package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation

class HasMeasurableDisease : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val hasMeasurableDisease = record.tumor.hasMeasurableDisease
            ?: return EvaluationFactory.recoverableUndetermined("Undetermined measurable disease (data missing)")
        return if (hasMeasurableDisease) {
            EvaluationFactory.recoverablePass("Has measurable disease")
        } else {
            EvaluationFactory.recoverableFail("No measurable disease")
        }
    }
}