package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.doid.DoidConstants
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.doid.DoidModel

class HasCancerWithSmallCellComponent (private val doidModel: DoidModel) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val tumorDoids = record.clinical().tumor().doids()
        if (!DoidEvaluationFunctions.hasConfiguredDoids(tumorDoids) && record.clinical().tumor().primaryTumorExtraDetails() == null) {
            return EvaluationFactory.undetermined(
                "Could not determine whether tumor of patient may have a small component",
                "Undetermined small cell component"
            )
        }
        val hasSmallCellDoid = DoidEvaluationFunctions.isOfAtLeastOneDoidType(doidModel, tumorDoids, SMALL_CELL_DOIDS)
        val hasSmallCellTerm = DoidEvaluationFunctions.isOfAtLeastOneDoidTerm(doidModel, tumorDoids, SMALL_CELL_TERMS)
        val hasSmallCellDetails = TumorTypeEvaluationFunctions.hasTumorWithDetails(record.clinical().tumor(), SMALL_CELL_EXTRA_DETAILS)
        return if (hasSmallCellDoid || hasSmallCellTerm || hasSmallCellDetails) {
            EvaluationFactory.pass(
                "Patient has cancer with small cell component",
                "Presence of small cell component"
            )
        } else
            EvaluationFactory.fail(
                "Patient does not have cancer with small cell component",
                "No small cell component"
            )
    }

    companion object {
        val SMALL_CELL_DOIDS = setOf(DoidConstants.SMALL_CELL_CARCINOMA_DOID)
        val SMALL_CELL_TERMS = setOf("small cell")
        val SMALL_CELL_EXTRA_DETAILS = setOf("small cell", "SCNEC")
    }
}