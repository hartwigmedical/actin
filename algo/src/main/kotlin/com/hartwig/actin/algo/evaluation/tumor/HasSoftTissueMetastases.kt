package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.EvaluationLabels
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation

class HasSoftTissueMetastases(private val labels: EvaluationLabels.Tumor) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val certainSoftTissueMetastasesEvaluation = TumorEvaluationFunctions.hasSoftTissueMetastases(record.tumor)
        val suspectedSoftTissueMetastasesEvaluation = TumorEvaluationFunctions.hasSuspectedSoftTissueMetastases(record.tumor)

        return when {
            certainSoftTissueMetastasesEvaluation == null && suspectedSoftTissueMetastasesEvaluation != true -> {
                EvaluationFactory.undetermined(labels.hasSoftTissueMetastasesUndetermined())
            }

            certainSoftTissueMetastasesEvaluation == true -> EvaluationFactory.pass(labels.hasSoftTissueMetastasesPass())

            suspectedSoftTissueMetastasesEvaluation == true -> EvaluationFactory.warn(labels.hasSoftTissueMetastasesWarn())

            else -> EvaluationFactory.fail(labels.hasSoftTissueMetastasesFail())
        }
    }
}