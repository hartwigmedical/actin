package com.hartwig.actin.algo.evaluation.laboratory

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.laboratory.LabEvaluation.evaluateVersusMaxValueWithMargin
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

        return when (evaluateVersusMaxValueWithMargin(convertedValue, labValue.comparator, maxValue)) {
            LabEvaluation.LabEvaluationResult.EXCEEDS_THRESHOLD_AND_OUTSIDE_MARGIN -> {
                EvaluationFactory.recoverableFail(
                    "${labMeasurement.display().replaceFirstChar { it.uppercase() }} ${
                        String.format("%.1f", convertedValue)
                    } ${targetUnit.display()} exceeds maximum of $maxValue ${targetUnit.display()}",
                    "${labMeasurement.display().replaceFirstChar { it.uppercase() }} ${
                        String.format("%.1f", convertedValue)
                    } ${targetUnit.display()} exceeds max of $maxValue ${targetUnit.display()}"
                )
            }
            LabEvaluation.LabEvaluationResult.EXCEEDS_THRESHOLD_BUT_WITHIN_MARGIN -> {
                EvaluationFactory.recoverableUndetermined(
                    "${labMeasurement.display().replaceFirstChar { it.uppercase() }} ${
                        String.format("%.1f", convertedValue)
                    } ${targetUnit.display()} exceeds maximum of $maxValue ${targetUnit.display()} but within margin of error",
                    "${labMeasurement.display().replaceFirstChar { it.uppercase() }} ${
                        String.format("%.1f", convertedValue)
                    } ${targetUnit.display()} exceeds max of $maxValue ${targetUnit.display()} but within margin of error"
                )
            }
            LabEvaluation.LabEvaluationResult.CANNOT_BE_DETERMINED -> {
                EvaluationFactory.recoverableUndetermined(
                    "${labMeasurement.display().replaceFirstChar { it.uppercase() }} requirements could not be determined",
                    "${labMeasurement.display().replaceFirstChar { it.uppercase() }} requirements undetermined"
                )
            }
            LabEvaluation.LabEvaluationResult.WITHIN_THRESHOLD -> {
                EvaluationFactory.recoverablePass(
                    "${labMeasurement.display().replaceFirstChar { it.uppercase() }} ${
                        String.format("%.1f", convertedValue)
                    } below maximum of $maxValue ${targetUnit.display()}",
                    "${labMeasurement.display().replaceFirstChar { it.uppercase() }} ${
                        String.format("%.1f", convertedValue)
                    } below max of $maxValue ${targetUnit.display()}"
                )
            }
        }
    }
}