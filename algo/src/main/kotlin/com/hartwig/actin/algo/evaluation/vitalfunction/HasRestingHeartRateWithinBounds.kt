package com.hartwig.actin.algo.evaluation.vitalfunction

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.clinical.datamodel.VitalFunctionCategory

class HasRestingHeartRateWithinBounds(private val minMedianRestingHeartRate: Double, private val maxMedianRestingHeartRate: Double) :
    EvaluationFunction {
    override fun evaluate(record: PatientRecord): Evaluation {
        val heartRates = VitalFunctionSelector.select(
            record.clinical().vitalFunctions(),
            VitalFunctionCategory.HEART_RATE,
            UNIT_TO_SELECT,
            MAX_HEART_RATES_TO_USE
        )
        if (heartRates.isEmpty()) {
            return EvaluationFactory.recoverableUndetermined("No heart rate data found")
        }
        val median = VitalFunctionFunctions.determineMedianValue(heartRates)
        return if (median.compareTo(minMedianRestingHeartRate) >= 0 && median.compareTo(maxMedianRestingHeartRate) <= 0) {
            EvaluationFactory.recoverablePass(
                "Patient has median heart rate between $minMedianRestingHeartRate and $maxMedianRestingHeartRate",
                "Heart rate within range"
            )
        } else {
            EvaluationFactory.recoverableFail(
                "Patient does not have median heart rate between $minMedianRestingHeartRate and $maxMedianRestingHeartRate",
                "Heart rate outside range"
            )
        }
    }

    companion object {
        const val UNIT_TO_SELECT: String = "BPM"
        private const val MAX_HEART_RATES_TO_USE = 5
    }
}