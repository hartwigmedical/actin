package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.EvaluationLabels
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation

class HasMeasurableDisease(private val labels: EvaluationLabels.Tumor) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val hasMeasurableDisease = record.tumor.hasMeasurableDisease
            ?: return EvaluationFactory.recoverableUndetermined(labels.hasMeasurableDiseaseRecoverableUndetermined())
        return if (hasMeasurableDisease) {
            EvaluationFactory.recoverablePass(labels.hasMeasurableDiseasePass())
        } else {
            EvaluationFactory.recoverableFail(labels.hasMeasurableDiseaseRecoverableFail())
        }
    }
}