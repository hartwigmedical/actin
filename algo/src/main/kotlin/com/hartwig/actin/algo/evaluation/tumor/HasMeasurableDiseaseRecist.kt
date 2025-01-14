package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.doid.DoidConstants
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.doid.DoidModel

class HasMeasurableDiseaseRecist(private val doidModel: DoidModel) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val hasMeasurableDisease = record.tumor.hasMeasurableDisease
            ?: return EvaluationFactory.recoverableUndetermined("Measurable disease by RECIST undetermined (data missing)")

        return if (hasMeasurableDisease && DoidEvaluationFunctions.isOfAtLeastOneDoidType(
                doidModel,
                record.tumor.doids,
                NON_RECIST_TUMOR_DOIDS
            )
        ) {
            EvaluationFactory.warn("Has measurable disease but with this tumor type unknown if by RECIST")
        } else if (hasMeasurableDisease) {
            EvaluationFactory.recoverablePass("Has measurable disease")
        } else {
            EvaluationFactory.recoverableFail("Has no measurable disease")
        }
    }

    companion object {
        val NON_RECIST_TUMOR_DOIDS = setOf(
            DoidConstants.HEMATOLOGIC_CANCER_DOID,
            DoidConstants.BRAIN_CANCER_DOID,
            DoidConstants.LYMPHOMA_DOID,
            DoidConstants.MULTIPLE_MYELOMA_DOID
        )
    }
}