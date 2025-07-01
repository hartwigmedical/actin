package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.doid.DoidModel

class HasCancerWithLargeCellComponent(private val doidModel: DoidModel) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val tumorDoids = record.tumor.doids
        if (!DoidEvaluationFunctions.hasConfiguredDoids(tumorDoids)) {
            return EvaluationFactory.undetermined("Undetermined if tumor may have large cell component")
        }

        return when {
            TumorEvaluationFunctions.hasTumorWithLargeCellComponent(doidModel, tumorDoids, record.tumor.name) -> {
                EvaluationFactory.pass("Has cancer with large cell component")
            }

            TumorEvaluationFunctions.hasTumorWithNeuroendocrineComponent(doidModel, tumorDoids, record.tumor.name) -> {
                EvaluationFactory.undetermined("Potentially has cancer with large cell component (has neuroendocrine tumor type)")
            }

            else -> {
                EvaluationFactory.fail("Has no cancer with large cell component")
            }
        }
    }
}