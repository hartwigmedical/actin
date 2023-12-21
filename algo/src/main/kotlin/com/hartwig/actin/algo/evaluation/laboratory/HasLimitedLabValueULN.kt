package com.hartwig.actin.algo.evaluation.laboratory

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationFactory.recoverable
import com.hartwig.actin.clinical.datamodel.LabValue
import com.hartwig.actin.clinical.interpretation.LabMeasurement

class HasLimitedLabValueULN internal constructor(private val maxULNFactor: Double) : LabEvaluationFunction {
    override fun evaluate(record: PatientRecord, labMeasurement: LabMeasurement, labValue: LabValue): Evaluation {
        val result = LabEvaluation.evaluateVersusMaxULN(labValue, maxULNFactor)
        val builder = recoverable().result(result)
        when (result) {
            EvaluationResult.FAIL -> {
                builder.addFailSpecificMessages(
                    "${labMeasurement.display().replaceFirstChar { it.uppercase() }} ${
                        String.format(
                            "%.1f",
                            labValue.value()
                        )
                    } exceeds maximum of $maxULNFactor*ULN ($maxULNFactor*${labValue.refLimitUp()})"
                )
                builder.addFailGeneralMessages(
                    "${labMeasurement.display().replaceFirstChar { it.uppercase() }} ${
                        String.format(
                            "%.1f",
                            labValue.value()
                        )
                    } exceeds max of $maxULNFactor*ULN ($maxULNFactor*${labValue.refLimitUp()})"
                )
            }

            EvaluationResult.UNDETERMINED -> {
                builder.addUndeterminedSpecificMessages(
                    "${
                        labMeasurement.display().replaceFirstChar { it.uppercase() }
                    } could not be evaluated against maximum ULN"
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
                    } below maximum of $maxULNFactor*ULN ($maxULNFactor*${labValue.refLimitUp()})"
                )
                builder.addPassGeneralMessages(
                    "${labMeasurement.display().replaceFirstChar { it.uppercase() }} ${
                        String.format(
                            "%.1f",
                            labValue.value()
                        )
                    } below max of $maxULNFactor*ULN ($maxULNFactor*${labValue.refLimitUp()})"
                )
            }

            else -> {}
        }
        return builder.build()
    }
}