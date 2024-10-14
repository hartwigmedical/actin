package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.doid.DoidConstants
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.doid.DoidModel

private const val TUMOR_SUB_LOCATION_SIDE_TEMPLATE = "Tumor sub-location %s is on %s side"

class HasLeftSidedColorectalTumor(private val doidModel: DoidModel) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val tumorDoids = record.tumor.doids
        return if (!DoidEvaluationFunctions.hasConfiguredDoids(tumorDoids)) {
            EvaluationFactory.undetermined("Unable to identify tumor type", "Unable to identify tumor type")
        } else if (!DoidEvaluationFunctions.isOfDoidType(doidModel, tumorDoids, DoidConstants.COLORECTAL_CANCER_DOID)) {
            EvaluationFactory.fail("Tumor is not colorectal cancer", "No CRC")
        } else {
            val subLocation = record.tumor.primaryTumorSubLocation?.lowercase()
            when {
                subLocation.isNullOrEmpty() -> EvaluationFactory.undetermined(
                    "Tumor sub-location unknown, left-sidedness is unknown", "Unknown sidedness of tumor"
                )

                LEFT_SUB_LOCATIONS.any(subLocation::contains) ->
                    EvaluationFactory.pass(String.format(TUMOR_SUB_LOCATION_SIDE_TEMPLATE, subLocation, "left"), "Left-sided CRC tumor")

                RIGHT_SUB_LOCATIONS.any(subLocation::contains) ->
                    EvaluationFactory.fail(String.format(TUMOR_SUB_LOCATION_SIDE_TEMPLATE, subLocation, "right"), "Right-sided CRC tumor")

                else -> EvaluationFactory.undetermined(
                    "Unknown tumor sub-location $subLocation, left-sidedness is unknown",
                    "Unknown sidedness of tumor"
                )
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