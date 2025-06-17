package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.doid.DoidConstants
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.doid.DoidModel

class HasOvarianBorderlineTumor(private val doidModel: DoidModel) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val tumorDoids = record.tumor.doids
        if (!DoidEvaluationFunctions.hasConfiguredDoids(tumorDoids)) {
            return EvaluationFactory.undetermined("Ovarian borderline tumor undetermined (no DOIDs)")
        }
        val isOvarianCancer = DoidEvaluationFunctions.isOfDoidType(doidModel, tumorDoids, DoidConstants.OVARIAN_CANCER_DOID)
        val hasBorderlineType = BORDERLINE_TERMS.any { record.tumor.name.lowercase().contains(it) }
        val hasGeneralOvarianCancer =
            DoidEvaluationFunctions.isOfExactDoid(tumorDoids, DoidConstants.OVARIAN_CANCER_DOID) || DoidEvaluationFunctions.isOfExactDoid(
                tumorDoids,
                DoidConstants.OVARIAN_CARCINOMA_DOID
            )

        return when {
            isOvarianCancer && hasBorderlineType -> EvaluationFactory.pass("Has ovarian borderline tumor")
            hasGeneralOvarianCancer -> EvaluationFactory.warn("Has ovarian cancer - undetermined if may be a borderline tumor")
            else -> EvaluationFactory.fail("Has no ovarian borderline tumor")
        }
    }

    companion object {
        val BORDERLINE_TERMS = setOf("borderline")
    }
}