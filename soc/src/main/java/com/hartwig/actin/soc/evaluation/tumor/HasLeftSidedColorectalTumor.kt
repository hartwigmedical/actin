package com.hartwig.actin.soc.evaluation.tumor

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.doid.DoidConstants
import java.util.*

class HasLeftSidedColorectalTumor internal constructor(doidModel: DoidModel) : EvaluationFunction {
    private val doidModel: DoidModel

    init {
        this.doidModel = doidModel
    }

    fun evaluate(record: PatientRecord): Evaluation {
        val tumorDoids = record.clinical().tumor().doids()
        return if (!DoidEvaluationFunctions.hasConfiguredDoids(tumorDoids)) {
            EvaluationFactory.undetermined("Unable to identify tumor type", "Tumor type")
        } else if (!DoidEvaluationFunctions.isOfDoidType(doidModel, tumorDoids, DoidConstants.COLORECTAL_CANCER_DOID)) {
            EvaluationFactory.fail("Tumor is not colorectal cancer", "Tumor type")
        } else {
            Optional.ofNullable(record.clinical().tumor().primaryTumorSubLocation())
                    .filter { subLocation: String -> !subLocation.isEmpty() }
                    .map { obj: String -> obj.lowercase(Locale.getDefault()) }
                    .map<Any> { subLocation: String ->
                        if (LEFT_SUB_LOCATIONS.stream().anyMatch { s: String? -> subLocation.contains(s!!) }) {
                            return@map EvaluationFactory.pass(String.format("Tumor sub-location %s is on left side", subLocation),
                                    "Tumor location")
                        } else if (RIGHT_SUB_LOCATIONS.stream().anyMatch { s: String? -> subLocation.contains(s!!) }) {
                            return@map EvaluationFactory.fail(String.format("Tumor sub-location %s is on right side", subLocation),
                                    "Tumor location")
                        } else {
                            return@map EvaluationFactory.undetermined("Unknown tumor sub-location $subLocation", "Tumor location")
                        }
                    }
                    .orElse(EvaluationFactory.undetermined("Tumor sub-location not provided", "Tumor location"))
        }
    }

    companion object {
        private val LEFT_SUB_LOCATIONS = setOf("rectum", "descending colon", "colon sigmoid", "colon descendens", "rectosigmoid")
        private val RIGHT_SUB_LOCATIONS = setOf("ascending colon",
                "colon ascendens",
                "caecum",
                "cecum",
                "transverse colon",
                "colon transversum",
                "flexura hepatica",
                "hepatic flexure")
    }
}