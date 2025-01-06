package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation

class HasUnresectablePeritonealMetastases : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val certainPeritonealMetastasesEvaluation = TumorTypeEvaluationFunctions.hasPeritonealMetastases(record.tumor)
        val suspectedPeritonealMetastasesEvaluation = TumorTypeEvaluationFunctions.hasSuspectedPeritonealMetastases(record.tumor)

        return when {
            certainPeritonealMetastasesEvaluation == null && suspectedPeritonealMetastasesEvaluation != true -> {
                EvaluationFactory.undetermined("Unresectable peritoneal metastases undetermined (metastases data missing)")
            }

            certainPeritonealMetastasesEvaluation == true || suspectedPeritonealMetastasesEvaluation == true -> {
                val suspectedString = if (certainPeritonealMetastasesEvaluation != true) " (suspected)" else ""
                EvaluationFactory.warn("Undetermined if$suspectedString peritoneal metastases are unresectable")
            }

            else -> {
                EvaluationFactory.fail("No unresectable peritoneal metastases")
            }
        }
    }
}