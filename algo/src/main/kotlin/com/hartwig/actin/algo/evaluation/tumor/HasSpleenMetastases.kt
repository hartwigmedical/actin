package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.EvaluationLabels
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation

class HasSpleenMetastases(private val labels: EvaluationLabels.Tumor) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val certainSpleenMetastasesEvaluation = TumorEvaluationFunctions.hasSpleenMetastases(record.tumor)
        val suspectedSpleenMetastasesEvaluation = TumorEvaluationFunctions.hasSuspectedSpleenMetastases(record.tumor)

        return when {
            certainSpleenMetastasesEvaluation == null && suspectedSpleenMetastasesEvaluation != true -> {
                EvaluationFactory.undetermined(labels.hasSpleenMetastasesUndetermined())
            }

            certainSpleenMetastasesEvaluation == true -> EvaluationFactory.pass(labels.hasSpleenMetastasesPass())

            suspectedSpleenMetastasesEvaluation == true -> EvaluationFactory.warn(labels.hasSpleenMetastasesWarn())

            else -> EvaluationFactory.fail(labels.hasSpleenMetastasesFail())
        }
    }
}