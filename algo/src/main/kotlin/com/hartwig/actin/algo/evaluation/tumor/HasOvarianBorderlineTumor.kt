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
        if (!DoidEvaluationFunctions.hasConfiguredDoids(tumorDoids) || (record.tumor.primaryTumorType == null
                    && record.tumor.primaryTumorSubType == null)
        ) {
            return EvaluationFactory.undetermined("Ovarian borderline tumor undetermined (tumor type missing)")
        }
        val isOvarianCancer = DoidEvaluationFunctions.isOfDoidType(doidModel, tumorDoids, DoidConstants.OVARIAN_CANCER_DOID)
        val hasBorderlineType = TumorTypeEvaluationFunctions.hasTumorWithType(record.tumor, OVARIAN_BORDERLINE_TYPES)
        return if (isOvarianCancer && hasBorderlineType) {
            EvaluationFactory.pass("Has ovarian borderline tumor")
        } else
            EvaluationFactory.fail("Has no ovarian borderline tumor")
    }

    companion object {
        val OVARIAN_BORDERLINE_TYPES = setOf("Borderline tumor", "Borderline ovarian tumor")
    }
}