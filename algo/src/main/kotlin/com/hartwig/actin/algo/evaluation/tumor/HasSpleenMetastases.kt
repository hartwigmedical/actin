package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation

class HasSpleenMetastases : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val certainSpleenMetastasesEvaluation = TumorEvaluationFunctions.hasSpleenMetastases(record.tumor)
        val suspectedSpleenMetastasesEvaluation = TumorEvaluationFunctions.hasSuspectedSpleenMetastases(record.tumor)

        return when {
            certainSpleenMetastasesEvaluation == null && suspectedSpleenMetastasesEvaluation != true -> {
                EvaluationFactory.undetermined("Spleen metastases undetermined (metastases data missing)")
            }

            certainSpleenMetastasesEvaluation == true -> EvaluationFactory.pass("Has spleen metastases")

            suspectedSpleenMetastasesEvaluation == true -> EvaluationFactory.warn("Has suspected spleen metastases")

            else -> EvaluationFactory.fail("No spleen metastases")
        }
    }
}