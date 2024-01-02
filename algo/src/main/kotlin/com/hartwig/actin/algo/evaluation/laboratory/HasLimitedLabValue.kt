package com.hartwig.actin.algo.evaluation.laboratory

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationFactory.recoverable
import com.hartwig.actin.algo.evaluation.util.ValueComparison.evaluateVersusMaxValue
import com.hartwig.actin.clinical.datamodel.LabUnit
import com.hartwig.actin.clinical.datamodel.LabValue
import com.hartwig.actin.clinical.interpretation.LabMeasurement

class HasLimitedLabValue(private val maxValue: Double, private val measurement: LabMeasurement, private val targetUnit: LabUnit) :
    LabEvaluationFunction {
    override fun evaluate(record: PatientRecord, labMeasurement: LabMeasurement, labValue: LabValue): Evaluation {
        val convertedValue = LabUnitConverter.convert(measurement, labValue, targetUnit)
            ?: return recoverable()
                .result(EvaluationResult.UNDETERMINED)
                .addUndeterminedSpecificMessages("Could not convert value for ${labMeasurement.display()} to ${targetUnit.display()}")
                .build()
        val result = evaluateVersusMaxValue(convertedValue, labValue.comparator(), maxValue)
        val builder = recoverable().result(result)
        when (result) {
            EvaluationResult.FAIL -> {
                builder.addFailSpecificMessages(
                    "${labMeasurement.display().replaceFirstChar { it.uppercase() }} ${
                        String.format(
                            "%.1f",
                            convertedValue
                        )
                    } ${targetUnit.display()} exceeds maximum of $maxValue ${targetUnit.display()}"
                )
                builder.addFailGeneralMessages(
                    "${labMeasurement.display().replaceFirstChar { it.uppercase() }} ${
                        String.format(
                            "%.1f",
                            convertedValue
                        )
                    } ${targetUnit.display()} exceeds max of $maxValue ${targetUnit.display()}"
                )
            }

            EvaluationResult.UNDETERMINED -> {
                builder.addUndeterminedSpecificMessages(
                    "${
                        labMeasurement.display().replaceFirstChar { it.uppercase() }
                    } requirements could not be determined"
                )
                builder.addUndeterminedGeneralMessages(
                    "${
                        labMeasurement.display().replaceFirstChar { it.uppercase() }
                    } requirements undetermined"
                )
            }

            EvaluationResult.PASS -> {
                builder.addPassSpecificMessages(
                    "${labMeasurement.display().replaceFirstChar { it.uppercase() }} ${
                        String.format(
                            "%.1f",
                            convertedValue
                        )
                    } below maximum of $maxValue ${targetUnit.display()}"
                )
                builder.addPassGeneralMessages(
                    "${labMeasurement.display().replaceFirstChar { it.uppercase() }} ${
                        String.format(
                            "%.1f",
                            convertedValue
                        )
                    } below max of $maxValue ${targetUnit.display()}"
                )
            }

            else -> {}
        }
        return builder.build()
    }
}