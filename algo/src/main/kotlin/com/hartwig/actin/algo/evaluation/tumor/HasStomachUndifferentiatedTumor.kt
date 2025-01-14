package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.doid.DoidConstants
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.doid.DoidModel

class HasStomachUndifferentiatedTumor(private val doidModel: DoidModel) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val tumorDoids = record.tumor.doids
        if (!DoidEvaluationFunctions.hasConfiguredDoids(tumorDoids) || (record.tumor.primaryTumorType == null
                    && record.tumor.primaryTumorSubType == null)
        ) {
            return EvaluationFactory.undetermined("Undifferentiated stomach tumor undetermined (tumor type missing)")
        }
        val isStomachCancer = DoidEvaluationFunctions.isOfDoidType(doidModel, tumorDoids, DoidConstants.STOMACH_CANCER_DOID)
        val isUndifferentiatedType =
            TumorTypeEvaluationFunctions.hasTumorWithType(record.tumor, UNDIFFERENTIATED_TYPES) ||
                    TumorTypeEvaluationFunctions.hasTumorWithDetails(record.tumor, UNDIFFERENTIATED_DETAILS)
        return if (isStomachCancer && isUndifferentiatedType) {
            EvaluationFactory.pass("Has undifferentiated stomach tumor")
        } else
            EvaluationFactory.fail("No undifferentiated stomach tumor")
    }

    companion object {
        val UNDIFFERENTIATED_TYPES = setOf("Undifferentiated")
        val UNDIFFERENTIATED_DETAILS = setOf("Undifferentiated")
    }
}