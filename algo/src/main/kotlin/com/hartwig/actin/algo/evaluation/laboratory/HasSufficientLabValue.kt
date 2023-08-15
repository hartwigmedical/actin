package com.hartwig.actin.algo.evaluation.laboratory

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.evaluation.EvaluationFactory.recoverable
import com.hartwig.actin.algo.evaluation.util.ValueComparison.evaluateVersusMinValue
import com.hartwig.actin.clinical.datamodel.LabUnit
import com.hartwig.actin.clinical.datamodel.LabValue
import com.hartwig.actin.clinical.interpretation.LabMeasurement

class HasSufficientLabValue internal constructor(
    private val minValue: Double,
    private val measurement: LabMeasurement,
    private val targetUnit: LabUnit
) : LabEvaluationFunction {
    override fun evaluate(record: PatientRecord, labValue: LabValue): Evaluation {
        val convertedValue = LabUnitConverter.convert(measurement, labValue, targetUnit)
            ?: return recoverable()
                .result(EvaluationResult.UNDETERMINED)
                .addUndeterminedSpecificMessages("Could not convert value for ${labValue.code()} to ${targetUnit.display()}")
                .build()
        val result = evaluateVersusMinValue(convertedValue, labValue.comparator(), minValue)
        val builder = recoverable().result(result)
        when (result) {
            EvaluationResult.FAIL -> {
                builder.addFailSpecificMessages("${labValue.code()} $convertedValue $targetUnit is insufficient (below minimum of $minValue $targetUnit)")
                builder.addFailGeneralMessages("${labValue.code()} $convertedValue $targetUnit below minimum of $minValue $targetUnit")
            }

            EvaluationResult.UNDETERMINED -> {
                builder.addUndeterminedSpecificMessages("${labValue.code()} sufficiency could not be evaluated")
                builder.addUndeterminedGeneralMessages("${labValue.code()} undetermined")
            }

            EvaluationResult.PASS -> {
                builder.addPassSpecificMessages("${labValue.code()} is sufficient")
                builder.addPassGeneralMessages("${labValue.code()} sufficient")
            }

            else -> {}
        }
        return builder.build()
    }
}