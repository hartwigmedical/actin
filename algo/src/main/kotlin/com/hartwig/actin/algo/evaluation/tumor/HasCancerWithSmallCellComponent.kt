package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.doid.DoidConstants
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.EvaluationLabels
import com.hartwig.actin.algo.evaluation.IhcTestEvaluation
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.doid.DoidModel

class HasCancerWithSmallCellComponent(private val doidModel: DoidModel, private val labels: EvaluationLabels.Tumor) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val tumorDoids = record.tumor.doids
        if (!DoidEvaluationFunctions.hasConfiguredDoids(tumorDoids)) {
            return EvaluationFactory.undetermined(labels.hasCancerWithSmallCellComponentUndetermined())
        }
        val isNsclc = DoidEvaluationFunctions.isOfDoidType(doidModel, record.tumor.doids, DoidConstants.LUNG_NON_SMALL_CELL_CARCINOMA_DOID)
        val ihcTestEvaluation = IhcTestEvaluation.create(item = "SCLC transformation", ihcTests = record.ihcTests)

        return when {
            TumorEvaluationFunctions.hasTumorWithSmallCellComponent(doidModel, tumorDoids, record.tumor.name) -> {
                EvaluationFactory.pass(labels.hasCancerWithSmallCellComponentPass())
            }

            isNsclc && ihcTestEvaluation.hasCertainBroadPositiveResultsForItem() -> {
                EvaluationFactory.warn(labels.hasCancerWithSmallCellComponentWarnCertainPositive())
            }

            isNsclc && ihcTestEvaluation.hasPossiblePositiveResultsForItem() -> {
                EvaluationFactory.warn(labels.hasCancerWithSmallCellComponentWarnPossiblePositive())
            }

            TumorEvaluationFunctions.hasTumorWithNeuroendocrineComponent(doidModel, tumorDoids, record.tumor.name) -> {
                EvaluationFactory.undetermined(labels.hasCancerWithSmallCellComponentUndeterminedNeuroendocrine())
            }

            else -> EvaluationFactory.fail(labels.hasCancerWithSmallCellComponentFail())
        }
    }
}