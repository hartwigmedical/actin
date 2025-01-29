package com.hartwig.actin.algo.evaluation.laboratory

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.laboratory.LabEvaluation.evaluateVersusMinValueWithMargin
import com.hartwig.actin.algo.evaluation.util.Format.labValue
import com.hartwig.actin.clinical.interpretation.LabMeasurement
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.LabUnit
import com.hartwig.actin.datamodel.clinical.LabValue

class HasSufficientLabValue(
    private val minValue: Double, private val measurement: LabMeasurement, private val targetUnit: LabUnit
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
        val refString = "$minValue ${targetUnit.display()}"

        return when (evaluateVersusMinValueWithMargin(convertedValue, labValue.comparator, minValue)) {
            LabEvaluation.LabEvaluationResult.EXCEEDS_THRESHOLD_AND_OUTSIDE_MARGIN -> {
                EvaluationFactory.recoverableFail("$labValueString below min of $refString")
            }

            LabEvaluation.LabEvaluationResult.EXCEEDS_THRESHOLD_BUT_WITHIN_MARGIN -> {
                EvaluationFactory.recoverableUndetermined("$labValueString below min of $refString but within margin of error")
            }

            LabEvaluation.LabEvaluationResult.CANNOT_BE_DETERMINED -> {
                EvaluationFactory.recoverableUndetermined("${labMeasurement.display().replaceFirstChar { it.uppercase() }} undetermined")
            }

            LabEvaluation.LabEvaluationResult.WITHIN_THRESHOLD -> {
                EvaluationFactory.recoverablePass("$labValueString exceeds min of $refString")
            }
        }
    }
}