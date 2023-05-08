package com.hartwig.actin.algo.evaluation.laboratory

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationFactory.recoverable
import com.hartwig.actin.clinical.datamodel.LabValue

class HasLimitedLabValueULN internal constructor(private val maxULNFactor: Double) : LabEvaluationFunction {
    override fun evaluate(record: PatientRecord, labValue: LabValue): Evaluation {
        val result = LabEvaluation.evaluateVersusMaxULN(labValue, maxULNFactor)
        val builder = recoverable().result(result)
        if (result == EvaluationResult.FAIL) {
            builder.addFailSpecificMessages(labValue.code() + " exceeds maximum ULN")
            builder.addFailGeneralMessages(labValue.code() + " exceeds maximum ULN")
        } else if (result == EvaluationResult.UNDETERMINED) {
            builder.addUndeterminedSpecificMessages(labValue.code() + " could not be evaluated against maximum ULN")
            builder.addUndeterminedGeneralMessages(labValue.code() + " undetermined")
        } else if (result == EvaluationResult.PASS) {
            builder.addPassSpecificMessages(labValue.code() + " does not exceed maximum ULN")
            builder.addPassGeneralMessages(labValue.code() + " within maximum ULN")
        }
        return builder.build()
    }
}