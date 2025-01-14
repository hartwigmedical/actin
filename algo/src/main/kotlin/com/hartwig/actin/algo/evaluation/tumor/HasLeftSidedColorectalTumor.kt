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
            val subLocation = record.tumor.primaryTumorSubLocation?.lowercase()
            when {
                subLocation.isNullOrEmpty() -> EvaluationFactory.undetermined("Unknown sidedness of CRC tumor (sublocation unknown)")

                LEFT_SUB_LOCATIONS.any(subLocation::contains) ->
                    EvaluationFactory.pass("Left-sided CRC tumor ($subLocation)")

                RIGHT_SUB_LOCATIONS.any(subLocation::contains) ->
                    EvaluationFactory.fail("Right-sided CRC tumor ($subLocation)")

                else -> EvaluationFactory.undetermined("Unknown sidedness of tumor for $subLocation tumor")
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