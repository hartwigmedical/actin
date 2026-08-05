package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.EvaluationLabels
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation

class HasUnresectablePeritonealMetastases(private val labels: EvaluationLabels.Tumor) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val certainPeritonealMetastasesEvaluation = TumorEvaluationFunctions.hasPeritonealMetastases(record.tumor)
        val suspectedPeritonealMetastasesEvaluation = TumorEvaluationFunctions.hasSuspectedPeritonealMetastases(record.tumor)

        return when {
            certainPeritonealMetastasesEvaluation == null && suspectedPeritonealMetastasesEvaluation != true -> {
                EvaluationFactory.undetermined(labels.hasUnresectablePeritonealMetastasesUndeterminedMissing())
            }

            certainPeritonealMetastasesEvaluation == true || suspectedPeritonealMetastasesEvaluation == true -> {
                val suspectedString =
                    if (certainPeritonealMetastasesEvaluation != true) labels.hasUnresectablePeritonealMetastasesSuffixSuspected() else ""
                EvaluationFactory.warn(labels.hasUnresectablePeritonealMetastasesWarn(suspectedString))
            }

            else -> {
                EvaluationFactory.fail(labels.hasUnresectablePeritonealMetastasesFail())
            }
        }
    }
}