package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.doid.DoidConstants
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.EvaluationLabels
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.doid.DoidModel

class HasLeftSidedColorectalTumor(private val doidModel: DoidModel, private val labels: EvaluationLabels.Tumor) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val tumorDoids = record.tumor.doids

        return if (!DoidEvaluationFunctions.hasConfiguredDoids(tumorDoids)) {
            EvaluationFactory.undetermined(labels.hasLeftSidedColorectalTumorUndetermined())
        } else if (!DoidEvaluationFunctions.isOfDoidType(doidModel, tumorDoids, DoidConstants.COLORECTAL_CANCER_DOID)) {
            EvaluationFactory.fail(labels.hasLeftSidedColorectalTumorFailNotColorectal())
        } else {
            val name = record.tumor.name
            when {
                LEFT_SUB_LOCATIONS.any { subLocation -> name.lowercase().split(Regex("\\W+")).contains(subLocation) } ->
                    EvaluationFactory.pass(labels.hasLeftSidedColorectalTumorPass(name))

                RIGHT_SUB_LOCATIONS.any(name.lowercase()::contains) ->
                    EvaluationFactory.fail(labels.hasLeftSidedColorectalTumorFail(name))

                else -> EvaluationFactory.undetermined(labels.hasLeftSidedColorectalTumorUndeterminedLocation(name))
            }
        }
    }

    companion object {
        val LEFT_SUB_LOCATIONS = setOf("rectum", "descending", "sigmoid", "descendens", "rectosigmoid")
        val RIGHT_SUB_LOCATIONS =
            setOf("ascending", "ascendens", "caecum", "cecum", "transverse", "transversum", "flexura hepatica", "hepatic flexure")
    }
}