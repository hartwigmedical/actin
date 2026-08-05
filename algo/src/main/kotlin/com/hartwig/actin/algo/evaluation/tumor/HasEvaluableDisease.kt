package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.EvaluationLabels
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation

class HasEvaluableDisease(private val labels: EvaluationLabels.Tumor) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        return when (record.tumor.hasMeasurableDisease) {
            true -> {
                EvaluationFactory.recoverablePass(labels.hasEvaluableDiseasePass())
            }
            else -> {
                EvaluationFactory.recoverableUndetermined(labels.hasEvaluableDiseaseRecoverableUndetermined())
            }
        }
    }
}