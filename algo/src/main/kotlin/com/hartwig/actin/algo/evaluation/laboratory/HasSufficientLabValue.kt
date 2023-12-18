package com.hartwig.actin.algo.evaluation.laboratory

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.util.ValueComparison.evaluateVersusMinValue
import com.hartwig.actin.clinical.datamodel.LabUnit
import com.hartwig.actin.clinical.datamodel.LabValue
import com.hartwig.actin.clinical.interpretation.LabMeasurement

class HasSufficientLabValue internal constructor(
    private val minValue: Double,
    private val measurement: LabMeasurement,
    private val targetUnit: LabUnit
) : LabEvaluationFunction {
    override fun evaluate(record: PatientRecord, labMeasurement: LabMeasurement, labValue: LabValue): Evaluation {
        val convertedValue = LabUnitConverter.convert(measurement, labValue, targetUnit)
            ?: return EvaluationFactory.recoverableUndetermined(
                "Could not convert value for ${labMeasurement.display()} to ${targetUnit.display()}"
            )
        val result = evaluateVersusMinValue(convertedValue, labValue.comparator(), minValue)
        val labValueString = "${labMeasurement.display()} ${String.format("%.1f", convertedValue)} ${targetUnit.display()}"
        val referenceString = "$minValue ${targetUnit.display()}"

        return when (result) {
            EvaluationResult.FAIL -> {
                EvaluationFactory.recoverableFail(
                    "$labValueString is below minimum of $referenceString", "$labValueString below min of $referenceString"
                )
            }
            EvaluationResult.UNDETERMINED -> {
                EvaluationFactory.recoverableUndetermined(
                    "${labMeasurement.display()} sufficiency could not be evaluated", "${labMeasurement.display()} undetermined"
                )
            }
            EvaluationResult.PASS -> {
                EvaluationFactory.recoverablePass(
                    "$labValueString exceeds minimum of $referenceString",
                    "${labMeasurement.display()} ${targetUnit.display()} exceeds min of $referenceString"
                )
            }

            else -> {
                Evaluation(result = result, recoverable = true)
            }
        }
    }
}