package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.doid.DoidConstants
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.EvaluationLabels
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.doid.DoidModel

class HasOvarianBorderlineTumor(private val doidModel: DoidModel, private val labels: EvaluationLabels.Tumor) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val tumorDoids = record.tumor.doids
        if (!DoidEvaluationFunctions.hasConfiguredDoids(tumorDoids)) {
            return EvaluationFactory.undetermined(labels.hasOvarianBorderlineTumorUndeterminedNoDoids())
        }
        val isOvarianCancer = DoidEvaluationFunctions.isOfDoidType(doidModel, tumorDoids, DoidConstants.OVARIAN_CANCER_DOID)
        val hasBorderlineType = BORDERLINE_TERMS.any { record.tumor.name.lowercase().contains(it) }
        val hasGeneralOvarianCancer =
            DoidEvaluationFunctions.isOfExactDoid(tumorDoids, DoidConstants.OVARIAN_CANCER_DOID) || DoidEvaluationFunctions.isOfExactDoid(
                tumorDoids,
                DoidConstants.OVARIAN_CARCINOMA_DOID
            )

        return when {
            isOvarianCancer && hasBorderlineType -> EvaluationFactory.pass(labels.hasOvarianBorderlineTumorPass())
            hasGeneralOvarianCancer -> EvaluationFactory.warn(labels.hasOvarianBorderlineTumorWarn())
            else -> EvaluationFactory.fail(labels.hasOvarianBorderlineTumorFail())
        }
    }

    companion object {
        val BORDERLINE_TERMS = setOf("borderline")
    }
}