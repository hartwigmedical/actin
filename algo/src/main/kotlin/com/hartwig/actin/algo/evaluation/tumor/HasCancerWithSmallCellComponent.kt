package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.doid.DoidConstants
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.IhcTestEvaluation
import com.hartwig.actin.algo.evaluation.IhcTestItemConstants
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.doid.DoidModel

class HasCancerWithSmallCellComponent(private val doidModel: DoidModel) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val tumorDoids = record.tumor.doids
        if (!DoidEvaluationFunctions.hasConfiguredDoids(tumorDoids)) {
            return EvaluationFactory.undetermined("Undetermined if cancer may have small cell component")
        }
        val isNsclc = DoidEvaluationFunctions.isOfDoidType(doidModel, record.tumor.doids, DoidConstants.LUNG_NON_SMALL_CELL_CARCINOMA_DOID)
        val ihcTestEvaluations =
            IhcTestItemConstants.SMALL_CELL_TRANSFORMATION_TERMS.map { IhcTestEvaluation.create(it, record.ihcTests) }

        return when {
            TumorEvaluationFunctions.hasTumorWithSmallCellComponent(doidModel, tumorDoids, record.tumor.name) -> {
                EvaluationFactory.pass("Cancer has small cell component")
            }

            isNsclc && ihcTestEvaluations.any(IhcTestEvaluation::hasCertainBroadPositiveResultsForItem) -> {
                EvaluationFactory.warn("Cancer potentially has small cell component (positive SCLC transformation)")
            }

            isNsclc && ihcTestEvaluations.any(IhcTestEvaluation::hasPossiblePositiveResultsForItem) -> {
                EvaluationFactory.warn("Cancer potentially has small cell component (possible SCLC transformation)")
            }

            TumorEvaluationFunctions.hasTumorWithNeuroendocrineComponent(doidModel, tumorDoids, record.tumor.name) -> {
                EvaluationFactory.undetermined("Cancer may have small cell component (has neuroendocrine component)")
            }

            else -> EvaluationFactory.fail("No cancer with small cell component")
        }
    }
}