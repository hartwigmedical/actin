package com.hartwig.actin.algo.evaluation.laboratory

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationFactory.recoverable
import com.hartwig.actin.clinical.datamodel.LabValue

class HasSufficientLabValueULN internal constructor(private val minULNFactor: Double) : LabEvaluationFunction {
    override fun evaluate(record: PatientRecord, labValue: LabValue): Evaluation {
        val result = LabEvaluation.evaluateVersusMinULN(labValue, minULNFactor)
        val builder = recoverable().result(result)
        when (result) {
            EvaluationResult.FAIL -> {
                builder.addFailSpecificMessages("${labValue.code()} is insufficient versus maximum ULN")
                builder.addFailGeneralMessages("${labValue.code()} insufficient")
            }

            EvaluationResult.UNDETERMINED -> {
                builder.addUndeterminedSpecificMessages("${labValue.code()} could not be evaluated versus maximum ULN")
                builder.addUndeterminedGeneralMessages("${labValue.code()} undetermined")
            }

            EvaluationResult.PASS -> {
                builder.addPassSpecificMessages("${labValue.code()} sufficient (exceeds maximum ULN)")
                builder.addPassGeneralMessages("${labValue.code()} sufficient")
            }

            else -> {}
        }
        return builder.build()
    }
}