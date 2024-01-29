package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.doid.DoidConstants
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.doid.DoidModel

class HasStomachUndifferentiatedTumor (private val doidModel: DoidModel) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val tumorDoids = record.clinical.tumor.doids
        if (!DoidEvaluationFunctions.hasConfiguredDoids(tumorDoids) || (record.clinical.tumor.primaryTumorType == null
                    && record.clinical.tumor.primaryTumorSubType == null)
        ) {
            return EvaluationFactory.undetermined(
                "Could not determine whether patient has undifferentiated stomach tumor",
                "Undetermined undifferentiated stomach tumor"
            )
        }
        val isStomachCancer = DoidEvaluationFunctions.isOfDoidType(doidModel, tumorDoids, DoidConstants.STOMACH_CANCER_DOID)
        val isUndifferentiatedType = TumorTypeEvaluationFunctions.hasTumorWithType(record.clinical.tumor, UNDIFFERENTIATED_TYPES)
        return if (isStomachCancer && isUndifferentiatedType) {
            EvaluationFactory.pass("Patient has undifferentiated stomach tumor", "Tumor type")
        } else
            EvaluationFactory.fail("Patient does not have undifferentiated stomach tumor", "Tumor type")
    }

    companion object {
        val UNDIFFERENTIATED_TYPES = setOf("Undifferentiated")
    }
}