package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.doid.DoidConstants
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.doid.DoidModel

class HasOvarianBorderlineTumor(private val doidModel: DoidModel) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val tumorDoids = record.tumor.doids
        if (!DoidEvaluationFunctions.hasConfiguredDoids(tumorDoids) || (record.tumor.primaryTumorType == null
                    && record.tumor.primaryTumorSubType == null)
        ) {
            return EvaluationFactory.undetermined(
                "Could not determine whether patient has ovarian borderline tumor",
                "Undetermined ovarian borderline tumor"
            )
        }
        val isOvarianCancer = DoidEvaluationFunctions.isOfDoidType(doidModel, tumorDoids, DoidConstants.OVARIAN_CANCER_DOID)
        val hasBorderlineType = TumorTypeEvaluationFunctions.hasTumorWithType(record.tumor, OVARIAN_BORDERLINE_TYPES)
        return if (isOvarianCancer && hasBorderlineType) {
            EvaluationFactory.pass("Patient has ovarian borderline tumor", "Tumor type")
        } else
            EvaluationFactory.fail("Patient does not have ovarian borderline tumor", "Tumor type")
    }

    companion object {
        val OVARIAN_BORDERLINE_TYPES = setOf("Borderline tumor", "Borderline ovarian tumor")
    }
}