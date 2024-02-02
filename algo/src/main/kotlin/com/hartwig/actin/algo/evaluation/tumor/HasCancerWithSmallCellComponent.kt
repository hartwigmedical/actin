package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.doid.DoidConstants
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.tumor.DoidEvaluationFunctions.isOfAtLeastOneDoidTerm
import com.hartwig.actin.algo.evaluation.tumor.DoidEvaluationFunctions.isOfAtLeastOneDoidType
import com.hartwig.actin.algo.evaluation.tumor.DoidEvaluationFunctions.isOfDoidType
import com.hartwig.actin.algo.evaluation.tumor.TumorTypeEvaluationFunctions.hasTumorWithDetails
import com.hartwig.actin.algo.evaluation.tumor.TumorTypeEvaluationFunctions.hasTumorWithType
import com.hartwig.actin.doid.DoidModel

class HasCancerWithSmallCellComponent (private val doidModel: DoidModel) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val tumorDoids = record.clinical.tumor.doids
        if (!DoidEvaluationFunctions.hasConfiguredDoids(tumorDoids) && record.clinical.tumor.primaryTumorExtraDetails == null) {
            return EvaluationFactory.undetermined(
                "Could not determine whether tumor of patient may have a small component",
                "Undetermined small cell component"
            )
        }
        val hasSmallCellComponent =
            isOfAtLeastOneDoidType(doidModel, tumorDoids, SMALL_CELL_DOIDS)
                    || isOfAtLeastOneDoidTerm(doidModel, tumorDoids, SMALL_CELL_TERMS)
                    || hasTumorWithType(record.clinical.tumor, SMALL_CELL_TERMS)
                    || hasTumorWithDetails(record.clinical.tumor, SMALL_CELL_EXTRA_DETAILS)

        return when {
            (hasSmallCellComponent) -> {
                EvaluationFactory.pass(
                    "Patient has cancer with small cell component",
                    "Presence of small cell component"
                )
            }

            WARN_DOIDS_SET.any { (isOfDoidType(doidModel, tumorDoids, it)) } -> {
                EvaluationFactory.warn(
                    "Patient has a neuroendocrine tumor type but it is undetermined if there is a small cell component",
                    "Neuroendocrine tumor type but undetermined if tumor has a small cell component"
                )
            }

            else -> {
                EvaluationFactory.fail(
                    "Patient does not have cancer with small cell component",
                    "No small cell component"
                )
            }
        }
    }

    companion object {
        val SMALL_CELL_DOIDS = setOf(DoidConstants.SMALL_CELL_CARCINOMA_DOID)
        val SMALL_CELL_TERMS = setOf("small cell")
        val SMALL_CELL_EXTRA_DETAILS = setOf("small cell", "SCNEC")
        val WARN_DOIDS_SET = setOf(DoidConstants.NEUROENDOCRINE_CARCINOMA_DOID, DoidConstants.NEUROENDOCRINE_TUMOR_DOID)
    }
}