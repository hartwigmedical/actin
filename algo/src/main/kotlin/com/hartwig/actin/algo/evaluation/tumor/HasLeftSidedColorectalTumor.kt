package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.doid.DoidConstants
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.doid.DoidModel

class HasLeftSidedColorectalTumor(private val doidModel: DoidModel) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val tumorDoids = record.tumor.doids
        return if (!DoidEvaluationFunctions.hasConfiguredDoids(tumorDoids)) {
            EvaluationFactory.undetermined("Undetermined if left sided CRC (unknown tumor type)")
        } else if (!DoidEvaluationFunctions.isOfDoidType(doidModel, tumorDoids, DoidConstants.COLORECTAL_CANCER_DOID)) {
            EvaluationFactory.fail("No CRC")
        } else {
            val name = record.tumor.name
            when {
                LEFT_SUB_LOCATIONS.any(name.lowercase()::contains) ->
                    EvaluationFactory.pass("Has left-sided CRC tumor ($name)")

                RIGHT_SUB_LOCATIONS.any(name.lowercase()::contains) ->
                    EvaluationFactory.fail("Has no left-sided CRC tumor but right-sided tumor ($name)")

                else -> EvaluationFactory.undetermined("Undetermined if tumor $name is left-sided")
            }
        }
    }

    companion object {
        private val LEFT_SUB_LOCATIONS = setOf("rectum", "descending", "sigmoid", "descendens", "rectosigmoid")
        private val RIGHT_SUB_LOCATIONS = setOf(
            "ascending", "ascendens", "caecum", "cecum", "transverse", "transversum", "flexura hepatica", "hepatic flexure"
        )
    }
}