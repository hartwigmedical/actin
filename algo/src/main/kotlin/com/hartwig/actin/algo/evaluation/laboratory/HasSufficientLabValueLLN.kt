package com.hartwig.actin.algo.evaluation.laboratory

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationFactory.recoverable
import com.hartwig.actin.clinical.datamodel.LabValue
import com.hartwig.actin.clinical.interpretation.LabMeasurement

class HasSufficientLabValueLLN internal constructor(private val minLLNFactor: Double) : LabEvaluationFunction {
    override fun evaluate(record: PatientRecord, labMeasurement: LabMeasurement, labValue: LabValue): Evaluation {
        val result = LabEvaluation.evaluateVersusMinLLN(labValue, minLLNFactor)
        val builder = recoverable().result(result)
        when (result) {
            EvaluationResult.FAIL -> {
                builder.addFailSpecificMessages(
                    "${labMeasurement.display().replaceFirstChar { it.uppercase() }} ${
                        String.format(
                            "%.1f",
                            labValue.value()
                        )
                    } is below minimum of $minLLNFactor*LLN ($minLLNFactor*${labValue.refLimitLow()})"
                )
                builder.addFailGeneralMessages(
                    "${labMeasurement.display().replaceFirstChar { it.uppercase() }} ${
                        String.format(
                            "%.1f",
                            labValue.value()
                        )
                    } below min of $minLLNFactor*LLN ($minLLNFactor*${labValue.refLimitLow()})"
                )
            }

            EvaluationResult.UNDETERMINED -> {
                builder.addUndeterminedSpecificMessages(
                    "${
                        labMeasurement.display().replaceFirstChar { it.uppercase() }
                    } could not be evaluated against minimal LLN"
                )
                builder.addUndeterminedGeneralMessages("${labMeasurement.display().replaceFirstChar { it.uppercase() }} undetermined")
            }

            EvaluationResult.PASS -> {
                builder.addPassSpecificMessages(
                    "${labMeasurement.display().replaceFirstChar { it.uppercase() }} ${
                        String.format(
                            "%.1f",
                            labValue.value()
                        )
                    } above minimum of $minLLNFactor*LLN ($minLLNFactor*${labValue.refLimitLow()})"
                )
                builder.addPassGeneralMessages(
                    "${labMeasurement.display().replaceFirstChar { it.uppercase() }} ${
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