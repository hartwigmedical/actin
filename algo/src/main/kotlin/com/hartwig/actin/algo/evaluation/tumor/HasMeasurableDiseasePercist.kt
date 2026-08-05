package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.doid.DoidConstants
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.EvaluationLabels
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.doid.DoidModel

class HasMeasurableDiseasePercist(private val doidModel: DoidModel, private val labels: EvaluationLabels.Tumor) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val hasMeasurableDisease = record.tumor.hasMeasurableDisease
            ?: return EvaluationFactory.recoverableUndetermined(labels.hasMeasurableDiseasePercistRecoverableUndetermined())

        return if (hasMeasurableDisease && DoidEvaluationFunctions.isOfAtLeastOneDoidType(
                doidModel,
                record.tumor.doids,
                NON_PERCIST_TUMOR_DOIDS
            ) && !DoidEvaluationFunctions.isOfAtLeastOneDoidType(
                doidModel,
                record.tumor.doids,
                setOf(DoidConstants.LYMPHOMA_DOID)
            )
        ) {
            EvaluationFactory.warn(labels.hasMeasurableDiseasePercistWarn())
        } else if (hasMeasurableDisease) {
            EvaluationFactory.recoverablePass(labels.hasMeasurableDiseasePercistPass())
        } else {
            EvaluationFactory.recoverableFail(labels.hasMeasurableDiseasePercistRecoverableFail())
        }
    }

    companion object {
        val NON_PERCIST_TUMOR_DOIDS = setOf(
            DoidConstants.HEMATOLOGIC_CANCER_DOID,
            DoidConstants.BRAIN_CANCER_DOID,
            DoidConstants.MULTIPLE_MYELOMA_DOID,
            DoidConstants.PROSTATE_CANCER_DOID,
            DoidConstants.NEUROENDOCRINE_TUMOR_DOID
        )
    }
}