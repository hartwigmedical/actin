package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.EvaluationLabels
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.doid.DoidModel

class HasCancerWithLargeCellComponent(private val doidModel: DoidModel, private val labels: EvaluationLabels.Tumor) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val tumorDoids = record.tumor.doids
        if (!DoidEvaluationFunctions.hasConfiguredDoids(tumorDoids)) {
            return EvaluationFactory.undetermined(labels.hasCancerWithLargeCellComponentUndetermined())
        }

        return when {
            TumorEvaluationFunctions.hasTumorWithLargeCellComponent(doidModel, tumorDoids, record.tumor.name) -> {
                EvaluationFactory.pass(labels.hasCancerWithLargeCellComponentPass())
            }

            TumorEvaluationFunctions.hasTumorWithNeuroendocrineComponent(doidModel, tumorDoids, record.tumor.name) -> {
                EvaluationFactory.undetermined(labels.hasCancerWithLargeCellComponentUndeterminedNeuroendocrine())
            }

            else -> {
                EvaluationFactory.fail(labels.hasCancerWithLargeCellComponentFail())
            }
        }
    }
}