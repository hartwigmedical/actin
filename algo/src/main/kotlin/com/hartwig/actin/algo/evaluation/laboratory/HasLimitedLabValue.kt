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
    override fun evaluate(record: PatientRecord, labValue: LabValue): Evaluation {
        val convertedValue = LabUnitConverter.convert(measurement, labValue, targetUnit)
            ?: return recoverable()
                .result(EvaluationResult.UNDETERMINED)
                .addUndeterminedSpecificMessages("Could not convert value for " + labValue.code() + " to " + targetUnit.display())
                .build()
        val result = evaluateVersusMaxValue(convertedValue, labValue.comparator(), maxValue)
        val builder = recoverable().result(result)
        when (result) {
            EvaluationResult.FAIL -> {
                builder.addFailSpecificMessages(labValue.code() + " exceeds limit")
                builder.addFailGeneralMessages(labValue.code() + " exceeds limit")
            }

            EvaluationResult.UNDETERMINED -> {
                builder.addUndeterminedSpecificMessages(labValue.code() + " sufficiency could not be evaluated")
                builder.addUndeterminedGeneralMessages(labValue.code() + " undetermined")
            }

            EvaluationResult.PASS -> {
                builder.addPassSpecificMessages(labValue.code() + " is within limit")
                builder.addPassGeneralMessages(labValue.code() + " within limit")
            }

            else -> {}
        }
        return builder.build()
    }
}