package com.hartwig.actin.algo.evaluation.laboratory

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationFactory.recoverable
import com.hartwig.actin.clinical.datamodel.LabValue

class HasSufficientLabValueLLN internal constructor(private val minLLNFactor: Double) : LabEvaluationFunction {
    override fun evaluate(record: PatientRecord, labValue: LabValue): Evaluation {
        val result = LabEvaluation.evaluateVersusMinLLN(labValue, minLLNFactor)
        val builder = recoverable().result(result)
        if (result == EvaluationResult.FAIL) {
            builder.addFailSpecificMessages(labValue.code() + " is below minimal LLN")
            builder.addFailGeneralMessages(labValue.code() + " below minimal LLN")
        } else if (result == EvaluationResult.UNDETERMINED) {
            builder.addUndeterminedSpecificMessages(labValue.code() + " could not be evaluated against minimal LLN")
            builder.addUndeterminedGeneralMessages(labValue.code() + " undetermined")
        } else if (result == EvaluationResult.PASS) {
            builder.addPassSpecificMessages(labValue.code() + " is sufficient (exceeds minimal LLN)")
            builder.addPassGeneralMessages(labValue.code() + " sufficient")
        }
        return builder.build()
    }
}