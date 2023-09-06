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
        when (result) {
            EvaluationResult.FAIL -> {
                builder.addFailSpecificMessages(
                    "${labValue.code()} ${
                        String.format(
                            "%.1f",
                            labValue.value()
                        )
                    } is below minimum of $minLLNFactor*LLN ($minLLNFactor*${labValue.refLimitLow()})"
                )
                builder.addFailGeneralMessages(
                    "${labValue.code()} ${
                        String.format(
                            "%.1f",
                            labValue.value()
                        )
                    } below min of $minLLNFactor*LLN ($minLLNFactor*${labValue.refLimitLow()})"
                )
            }

            EvaluationResult.UNDETERMINED -> {
                builder.addUndeterminedSpecificMessages("${labValue.code()} could not be evaluated against minimal LLN")
                builder.addUndeterminedGeneralMessages("${labValue.code()} undetermined")
            }

            EvaluationResult.PASS -> {
                builder.addPassSpecificMessages(
                    "${labValue.code()} ${
                        String.format(
                            "%.1f",
                            labValue.value()
                        )
                    } above minimum of $minLLNFactor*LLN ($minLLNFactor*${labValue.refLimitLow()})"
                )
                builder.addPassGeneralMessages(
                    "${labValue.code()} ${
                        String.format(
                            "%.1f",
                            labValue.value()
                        )
                    } above min of $minLLNFactor*LLN ($minLLNFactor*${labValue.refLimitLow()})"
                )
            }

            else -> {}
        }
        return builder.build()
    }
}