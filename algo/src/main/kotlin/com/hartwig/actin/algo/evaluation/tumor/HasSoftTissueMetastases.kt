package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation

class HasSoftTissueMetastases : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val certainSoftTissueMetastasesEvaluation = TumorEvaluationFunctions.hasSoftTissueMetastases(record.tumor)
        val suspectedSoftTissueMetastasesEvaluation = TumorEvaluationFunctions.hasSuspectedSoftTissueMetastases(record.tumor)

        return when {
            certainSoftTissueMetastasesEvaluation == null && suspectedSoftTissueMetastasesEvaluation != true -> {
                EvaluationFactory.undetermined("Soft tissue metastases undetermined (metastases data missing)")
            }

            certainSoftTissueMetastasesEvaluation == true -> EvaluationFactory.pass("Has soft tissue metastases")

            suspectedSoftTissueMetastasesEvaluation == true -> EvaluationFactory.warn("Has suspected soft tissue metastases")

            else -> EvaluationFactory.fail("No soft tissue metastases")
        }
    }
}