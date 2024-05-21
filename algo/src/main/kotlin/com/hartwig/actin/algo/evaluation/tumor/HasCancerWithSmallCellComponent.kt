package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.doid.DoidConstants
import com.hartwig.actin.algo.doid.DoidConstants.SMALL_CELL_DOID_SET
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.tumor.DoidEvaluationFunctions.isOfAtLeastOneDoidType
import com.hartwig.actin.algo.evaluation.tumor.DoidEvaluationFunctions.isOfDoidType
import com.hartwig.actin.algo.evaluation.tumor.TumorTypeEvaluationFunctions.hasTumorWithDetails
import com.hartwig.actin.algo.evaluation.tumor.TumorTypeEvaluationFunctions.hasTumorWithType
import com.hartwig.actin.doid.DoidModel

class HasCancerWithSmallCellComponent(private val doidModel: DoidModel) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val tumorDoids = record.tumor.doids
        if (!DoidEvaluationFunctions.hasConfiguredDoids(tumorDoids) && record.tumor.primaryTumorExtraDetails == null) {
            return EvaluationFactory.undetermined(
                "Could not determine whether tumor of patient may have a small component",
                "Undetermined small cell component"
            )
        }
        val hasSmallCellComponent =
            isOfAtLeastOneDoidType(doidModel, tumorDoids, SMALL_CELL_DOID_SET)
                    || (hasTumorWithType(record.tumor, SMALL_CELL_TUMOR_TYPE_TERMS) && !hasTumorWithType(
                record.tumor,
                setOf("non-small")
            ))
                    || hasTumorWithDetails(record.tumor, SMALL_CELL_EXTRA_DETAILS) && !hasTumorWithDetails(
                record.tumor,
                setOf("non-small")
            )

        return when {
            (hasSmallCellComponent) -> {
                EvaluationFactory.pass(
                    "Patient has cancer with small cell component",
                    "Has cancer with small cell component"
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
        val SMALL_CELL_TUMOR_TYPE_TERMS = setOf("small cell", "SCNEC")
        val SMALL_CELL_EXTRA_DETAILS = setOf("small cell", "SCNEC")
        val WARN_DOIDS_SET = setOf(DoidConstants.NEUROENDOCRINE_CARCINOMA_DOID, DoidConstants.NEUROENDOCRINE_TUMOR_DOID)
    }
}