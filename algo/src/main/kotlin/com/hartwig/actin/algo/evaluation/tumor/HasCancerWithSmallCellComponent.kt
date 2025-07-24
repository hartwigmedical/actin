package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.doid.DoidConstants
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.IhcTestEvaluation
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.doid.DoidModel

class HasCancerWithSmallCellComponent(private val doidModel: DoidModel) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val tumorDoids = record.tumor.doids
        if (!DoidEvaluationFunctions.hasConfiguredDoids(tumorDoids)) {
            return EvaluationFactory.undetermined("Undetermined if tumor may have small cell component")
        }
        val isNsclc = DoidEvaluationFunctions.isOfDoidType(doidModel, record.tumor.doids, DoidConstants.LUNG_NON_SMALL_CELL_CARCINOMA_DOID)
        val (hasCertainPositiveSclcTransformation, hasPossiblePositiveSclcTransformation) =
            IhcTestEvaluation.hasPositiveIhcTestResultsForItem(item = "SCLC transformation", ihcTests = record.ihcTests)

        return when {
            TumorEvaluationFunctions.hasTumorWithSmallCellComponent(doidModel, tumorDoids, record.tumor.name) -> {
                EvaluationFactory.pass("Has cancer with small cell component")
            }

            isNsclc && (hasCertainPositiveSclcTransformation || hasPossiblePositiveSclcTransformation) -> {
                EvaluationFactory.warn("Has possible cancer with small cell component (possible SCLC transformation)")
            }

            TumorEvaluationFunctions.hasTumorWithNeuroendocrineComponent(doidModel, tumorDoids, record.tumor.name) -> {
                EvaluationFactory.undetermined("Potentially has cancer with small cell component (has neuroendocrine tumor type)")
            }

            else -> EvaluationFactory.fail("Has no cancer with small cell component")
        }
    }
}