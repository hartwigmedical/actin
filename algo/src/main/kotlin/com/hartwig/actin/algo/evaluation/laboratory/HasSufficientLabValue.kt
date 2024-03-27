package com.hartwig.actin.algo.evaluation.laboratory

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.laboratory.LabEvaluation.LAB_VALUE_NEGATIVE_MARGIN_OF_ERROR
import com.hartwig.actin.algo.evaluation.util.ValueComparison.evaluateVersusMinValueWithMargin
import com.hartwig.actin.clinical.datamodel.LabUnit
import com.hartwig.actin.clinical.datamodel.LabValue
import com.hartwig.actin.clinical.interpretation.LabMeasurement

class HasSufficientLabValue(
    private val minValue: Double, private val measurement: LabMeasurement, private val targetUnit: LabUnit
) : LabEvaluationFunction {
    override fun evaluate(record: PatientRecord, labMeasurement: LabMeasurement, labValue: LabValue): Evaluation {
        val convertedValue = LabUnitConverter.convert(measurement, labValue, targetUnit)
            ?: return EvaluationFactory.recoverableUndetermined(
                "Could not convert value for ${labMeasurement.display()} to ${targetUnit.display()}"
            )
        val result = evaluateVersusMinValueWithMargin(convertedValue, labValue.comparator, minValue, LAB_VALUE_NEGATIVE_MARGIN_OF_ERROR)
        val labValueString = "${labMeasurement.display().replaceFirstChar { it.uppercase() }} ${
            String.format("%.1f", convertedValue)
        } ${targetUnit.display()}"
        val refString = "$minValue ${targetUnit.display()}"

        return when (result) {
            EvaluationResult.FAIL -> {
                EvaluationFactory.recoverableFail(
                    "$labValueString is below minimum of $refString", "$labValueString below min of $refString"
                )
            }
            EvaluationResult.WARN -> {
                EvaluationFactory.recoverableUndetermined(
                    "$labValueString is slightly below minimum of $refString", "$labValueString slightly below min of $refString"
                )
            }
            EvaluationResult.UNDETERMINED -> {
                EvaluationFactory.recoverableUndetermined(
                    "${labMeasurement.display().replaceFirstChar { it.uppercase() }} sufficiency could not be evaluated",
                    "${labMeasurement.display().replaceFirstChar { it.uppercase() }} undetermined"
                )
            }
            EvaluationResult.PASS -> {
                EvaluationFactory.recoverablePass(
                    "$labValueString exceeds minimum of $refString",
                    "${labMeasurement.display().replaceFirstChar { it.uppercase() }} ${targetUnit.display()} exceeds min of $refString"
                )
            }

            else -> {
                Evaluation(result = result, recoverable = true)
            }
        }
    }
}