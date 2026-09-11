package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.doid.DoidConstants
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.doid.DoidModel

class HasMeasurableDiseaseRano(private val doidModel: DoidModel) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val hasMeasurableDisease = record.tumor.hasMeasurableDisease
            ?: return EvaluationFactory.recoverableUndetermined("Measurable disease by RANO undetermined (data missing)")

        return when {
            (hasMeasurableDisease && DoidEvaluationFunctions.isOfDoidType(
                doidModel,
                record.tumor.doids,
                DoidConstants.CNS_CANCER_DOID
            )) -> {
                EvaluationFactory.recoverablePass("Disease is measurable")
            }

            hasMeasurableDisease -> {
                EvaluationFactory.warn("Disease is measurable but with this tumor type unknown if by RANO")
            }

            else -> {
                EvaluationFactory.recoverableFail("Disease is not measurable")
            }
        }
    }
}