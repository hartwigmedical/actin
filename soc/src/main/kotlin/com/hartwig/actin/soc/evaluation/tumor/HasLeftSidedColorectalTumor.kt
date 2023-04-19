package com.hartwig.actin.soc.evaluation.tumor

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.doid.DoidModel
import com.hartwig.actin.soc.doid.DoidConstants
import com.hartwig.actin.soc.evaluation.EvaluationFactory
import com.hartwig.actin.soc.evaluation.EvaluationFunction
import com.hartwig.actin.util.ApplicationConfig

private const val TUMOR_SUB_LOCATION_SIDE_TEMPLATE = "Tumor sub-location %s is on %s side"

class HasLeftSidedColorectalTumor internal constructor(doidModel: DoidModel) : EvaluationFunction {
    private val doidModel: DoidModel

    init {
        this.doidModel = doidModel
    }

    override fun evaluate(record: PatientRecord): Evaluation {
        val tumorDoids = record.clinical().tumor().doids()
        return if (!DoidEvaluationFunctions.hasConfiguredDoids(tumorDoids)) {
            EvaluationFactory.undetermined("Unable to identify tumor type", "Tumor type")
        } else if (!DoidEvaluationFunctions.isOfDoidType(doidModel, tumorDoids, DoidConstants.COLORECTAL_CANCER_DOID)) {
            EvaluationFactory.fail("Tumor is not colorectal cancer", "Tumor type")
        } else {
            val subLocation = record.clinical().tumor().primaryTumorSubLocation()?.lowercase(ApplicationConfig.LOCALE)
            when {
                subLocation.isNullOrEmpty() -> EvaluationFactory.undetermined("Tumor sub-location not provided", "Tumor location")
                stringContainsAnyMember(subLocation, LEFT_SUB_LOCATIONS) ->
                    EvaluationFactory.pass(String.format(TUMOR_SUB_LOCATION_SIDE_TEMPLATE, subLocation, "left"), "Tumor location")

                stringContainsAnyMember(subLocation, RIGHT_SUB_LOCATIONS) ->
                    EvaluationFactory.fail(String.format(TUMOR_SUB_LOCATION_SIDE_TEMPLATE, subLocation, "right"), "Tumor location")

                else -> EvaluationFactory.undetermined("Unknown tumor sub-location $subLocation", "Tumor location")
            }
        }
    }

    private fun stringContainsAnyMember(string: String, collection: Collection<String>): Boolean {
        return collection.any { string.contains(it) }
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