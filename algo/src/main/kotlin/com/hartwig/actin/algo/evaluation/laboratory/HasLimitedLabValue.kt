package com.hartwig.actin.algo.evaluation.laboratory

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.laboratory.LabEvaluation.LAB_VALUE_POSITIVE_MARGIN_OF_ERROR
import com.hartwig.actin.algo.evaluation.util.ValueComparison.evaluateVersusMaxValueWithMargin
import com.hartwig.actin.clinical.datamodel.LabUnit
import com.hartwig.actin.clinical.datamodel.LabValue
import com.hartwig.actin.clinical.interpretation.LabMeasurement

class HasLimitedLabValue(
    private val maxValue: Double, private val measurement: LabMeasurement, private val targetUnit: LabUnit
) : LabEvaluationFunction {

    override fun evaluate(record: PatientRecord, labMeasurement: LabMeasurement, labValue: LabValue): Evaluation {
        val convertedValue = LabUnitConverter.convert(measurement, labValue, targetUnit)
            ?: return EvaluationFactory.recoverableUndetermined(
                "Could not convert value for ${labMeasurement.display()} to ${targetUnit.display()}"
            )

        return when (val result =
            evaluateVersusMaxValueWithMargin(convertedValue, labValue.comparator, maxValue, LAB_VALUE_POSITIVE_MARGIN_OF_ERROR)
        ) {
            EvaluationResult.FAIL -> {
                EvaluationFactory.recoverableFail(
                    "${labMeasurement.display().replaceFirstChar { it.uppercase() }} ${
                        String.format("%.1f", convertedValue)
                    } ${targetUnit.display()} exceeds maximum of $maxValue ${targetUnit.display()}",
                    "${labMeasurement.display().replaceFirstChar { it.uppercase() }} ${
                        String.format("%.1f", convertedValue)
                    } ${targetUnit.display()} exceeds max of $maxValue ${targetUnit.display()}"
                )
            }
            EvaluationResult.WARN -> {
                EvaluationFactory.recoverableUndetermined(
                    "${labMeasurement.display().replaceFirstChar { it.uppercase() }} ${
                        String.format("%.1f", convertedValue)
                    } ${targetUnit.display()} slightly exceeds maximum of $maxValue ${targetUnit.display()}",
                    "${labMeasurement.display().replaceFirstChar { it.uppercase() }} ${
                        String.format("%.1f", convertedValue)
                    } ${targetUnit.display()} slightly exceeds max of $maxValue ${targetUnit.display()}"
                )
            }
            EvaluationResult.UNDETERMINED -> {
                EvaluationFactory.recoverableUndetermined(
                    "${labMeasurement.display().replaceFirstChar { it.uppercase() }} requirements could not be determined",
                    "${labMeasurement.display().replaceFirstChar { it.uppercase() }} requirements undetermined"
                )
            }
            EvaluationResult.PASS -> {
                EvaluationFactory.recoverablePass(
                    "${labMeasurement.display().replaceFirstChar { it.uppercase() }} ${
                        String.format("%.1f", convertedValue)
                    } below maximum of $maxValue ${targetUnit.display()}",
                    "${labMeasurement.display().replaceFirstChar { it.uppercase() }} ${
                        String.format("%.1f", convertedValue)
                    } below max of $maxValue ${targetUnit.display()}"
                )
            }

            else -> {
                Evaluation(result = result, recoverable = true)
            }
        }
    }
}