package com.hartwig.actin.algo.evaluation.laboratory

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.laboratory.LabEvaluation.evaluateVersusMaxValueWithMargin
import com.hartwig.actin.algo.evaluation.util.Format.labValue
import com.hartwig.actin.datamodel.clinical.LabMeasurement
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.LabUnit
import com.hartwig.actin.datamodel.clinical.LabValue

class HasLimitedLabValue(
    private val maxValue: Double, private val measurement: LabMeasurement, private val targetUnit: LabUnit
) : LabEvaluationFunction {

    override fun evaluate(record: PatientRecord, labMeasurement: LabMeasurement, labValue: LabValue): Evaluation {
        val convertedValue = LabUnitConverter.convert(measurement, labValue, targetUnit)
            ?: return EvaluationFactory.recoverableUndetermined(
                "Could not convert value for ${labMeasurement.display()} to ${targetUnit.display()}"
            )
        val labValueString = labValue(
            labMeasurement,
            convertedValue,
            targetUnit
        ) + (" (converted from: ${labValue.value} ${labValue.unit.display()})".takeIf { convertedValue != labValue.value } ?: "")
        val refString = "$maxValue ${targetUnit.display()}"

        return when (evaluateVersusMaxValueWithMargin(convertedValue, labValue.comparator, maxValue)) {
            LabEvaluation.LabEvaluationResult.EXCEEDS_THRESHOLD_AND_OUTSIDE_MARGIN -> {
                EvaluationFactory.recoverableFail("$labValueString exceeds max of $refString")
            }

            LabEvaluation.LabEvaluationResult.EXCEEDS_THRESHOLD_BUT_WITHIN_MARGIN -> {
                EvaluationFactory.recoverableUndetermined(
                    "$labValueString exceeds max of $refString but within margin of error"
                )
            }

            LabEvaluation.LabEvaluationResult.CANNOT_BE_DETERMINED -> {
                EvaluationFactory.recoverableUndetermined(
                    "${labMeasurement.display().replaceFirstChar { it.uppercase() }} requirements undetermined"
                )
            }

            LabEvaluation.LabEvaluationResult.WITHIN_THRESHOLD -> {
                EvaluationFactory.recoverablePass("$labValueString below max of $refString")
            }
        }
    }
}