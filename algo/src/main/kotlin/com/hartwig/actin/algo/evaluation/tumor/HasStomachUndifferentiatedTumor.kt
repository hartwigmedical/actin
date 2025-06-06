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
        if (!DoidEvaluationFunctions.hasConfiguredDoids(tumorDoids)) {
            return EvaluationFactory.undetermined("Undifferentiated stomach tumor undetermined (DOIDs missing)")
        }
        val isStomachCancer = DoidEvaluationFunctions.isOfDoidType(doidModel, tumorDoids, DoidConstants.STOMACH_CANCER_DOID)
        val isUndifferentiatedType = UNDIFFERENTIATED_KEYWORDS.any { record.tumor.name.lowercase().contains(it) }

        return when {
            isStomachCancer && isUndifferentiatedType -> EvaluationFactory.pass("Has undifferentiated stomach tumor")
            isStomachCancer -> EvaluationFactory.warn("Has stomach tumor but undetermined if undifferentiated")
            else -> EvaluationFactory.fail("No undifferentiated stomach tumor")
        }
    }

    companion object {
        val UNDIFFERENTIATED_KEYWORDS = setOf("undifferentiated")
    }
}