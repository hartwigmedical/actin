package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.doid.DoidConstants
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.doid.DoidModel

private const val TUMOR_SUB_LOCATION_SIDE_TEMPLATE = "Tumor sub-location %s is on %s side"

class HasLeftSidedColorectalTumor (doidModel: DoidModel) : EvaluationFunction {
    private val doidModel: DoidModel

    init {
        this.doidModel = doidModel
    }

    override fun evaluate(record: PatientRecord): Evaluation {
        val tumorDoids = record.clinical.tumor.doids
        return if (!DoidEvaluationFunctions.hasConfiguredDoids(tumorDoids)) {
            EvaluationFactory.undetermined("Unable to identify tumor type", "Tumor type")
        } else if (!DoidEvaluationFunctions.isOfDoidType(doidModel, tumorDoids, DoidConstants.COLORECTAL_CANCER_DOID)) {
            EvaluationFactory.fail("Tumor is not colorectal cancer", "Tumor type")
        } else {
            val subLocation = record.clinical.tumor.primaryTumorSubLocation?.lowercase()
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
        private val LEFT_SUB_LOCATIONS = setOf("rectum", "descending colon", "colon sigmoid", "colon descendens", "rectosigmoid")
        private val RIGHT_SUB_LOCATIONS = setOf(
            "ascending colon",
            "colon ascendens",
            "caecum",
            "cecum",
            "transverse colon",
            "colon transversum",
            "flexura hepatica",
            "hepatic flexure"
        )
    }
}