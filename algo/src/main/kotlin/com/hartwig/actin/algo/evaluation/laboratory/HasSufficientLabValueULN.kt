package com.hartwig.actin.algo.evaluation.laboratory

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationFactory.recoverable
import com.hartwig.actin.clinical.datamodel.LabValue
import com.hartwig.actin.clinical.interpretation.LabMeasurement

class HasSufficientLabValueULN internal constructor(private val minULNFactor: Double) : LabEvaluationFunction {
    override fun evaluate(record: PatientRecord, labMeasurement: LabMeasurement, labValue: LabValue): Evaluation {
        val result = LabEvaluation.evaluateVersusMinULN(labValue, minULNFactor)
        val builder = recoverable().result(result)
        when (result) {
            EvaluationResult.FAIL -> {
                builder.addFailSpecificMessages(
                    "${labMeasurement.display()} ${
                        String.format(
                            "%.1f",
                            labValue.value()
                        )
                    } is below minimum of $minULNFactor*ULN ($minULNFactor*${labValue.refLimitUp()})"
                )
                builder.addFailGeneralMessages(
                    "${labMeasurement.display()} ${
                        String.format(
                            "%.1f",
                            labValue.value()
                        )
                    } below min of $minULNFactor*ULN ($minULNFactor*${labValue.refLimitUp()})"
                )
            }

            EvaluationResult.UNDETERMINED -> {
                builder.addUndeterminedSpecificMessages("${labValue.code()} could not be evaluated versus maximum ULN")
                builder.addUndeterminedGeneralMessages("${labValue.code()} undetermined")
            }

            EvaluationResult.PASS -> {
                builder.addPassSpecificMessages(
                    "${labMeasurement.display()} ${
                        String.format(
                            "%.1f",
                            labValue.value()
                        )
                    } above minimum of $minULNFactor*ULN ($minULNFactor*${labValue.refLimitUp()})"
                )
                builder.addPassGeneralMessages(
                    "${labMeasurement.display()} ${
                        String.format(
                            "%.1f",
                            labValue.value()
                        )
                    } above min of $minULNFactor*ULN ($minULNFactor*${labValue.refLimitUp()})"
                )
            }

            else -> {}
        }
        return builder.build()
    }
}