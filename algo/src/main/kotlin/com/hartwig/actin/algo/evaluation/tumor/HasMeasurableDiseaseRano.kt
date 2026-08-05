package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.doid.DoidConstants
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.EvaluationLabels
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.doid.DoidModel

class HasMeasurableDiseaseRano(private val doidModel: DoidModel, private val labels: EvaluationLabels.Tumor) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val hasMeasurableDisease = record.tumor.hasMeasurableDisease
            ?: return EvaluationFactory.recoverableUndetermined(labels.hasMeasurableDiseaseRanoRecoverableUndetermined())

        return when {
            (hasMeasurableDisease && DoidEvaluationFunctions.isOfDoidType(
                doidModel,
                record.tumor.doids,
                DoidConstants.CNS_CANCER_DOID
            )) -> {
                EvaluationFactory.recoverablePass(labels.hasMeasurableDiseaseRanoPass())
            }

            hasMeasurableDisease -> {
                EvaluationFactory.warn(labels.hasMeasurableDiseaseRanoWarn())
            }

            else -> {
                EvaluationFactory.recoverableFail(labels.hasMeasurableDiseaseRanoRecoverableFail())
            }
        }
    }
}