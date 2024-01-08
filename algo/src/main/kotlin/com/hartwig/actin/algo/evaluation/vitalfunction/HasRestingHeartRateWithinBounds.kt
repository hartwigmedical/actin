package com.hartwig.actin.algo.evaluation.vitalfunction

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.clinical.datamodel.VitalFunctionCategory

class HasRestingHeartRateWithinBounds(private val minMedianRestingHeartRate: Double, private val maxMedianRestingHeartRate: Double) :
    EvaluationFunction {
    override fun evaluate(record: PatientRecord): Evaluation {
        val totalRecentHeartRates = VitalFunctionSelector.selectVitalFunctions(
            record.clinical().vitalFunctions(),
            VitalFunctionCategory.HEART_RATE
        )
        if (totalRecentHeartRates.isEmpty()) {
            return EvaluationFactory.recoverableUndetermined("No (recent) heart rate data found")
        } else if (totalRecentHeartRates.none { it.unit().uppercase() == HEART_RATE_EXPECTED_UNIT }) {
            return EvaluationFactory.recoverableUndetermined("Heart rates not measured in $HEART_RATE_EXPECTED_UNIT")
        }

        val relevantHeartRates = VitalFunctionSelector.selectMedianPerDay(
            record.clinical().vitalFunctions(),
            VitalFunctionCategory.HEART_RATE,
            HEART_RATE_EXPECTED_UNIT,
            MAX_HEART_RATES_TO_USE
        )
        val median = VitalFunctionFunctions.determineMedianValue(relevantHeartRates)
        return if (median.compareTo(minMedianRestingHeartRate) >= 0 && median.compareTo(maxMedianRestingHeartRate) <= 0) {
            EvaluationFactory.recoverablePass(
                "Patient has median heart rate of $median bpm - thus between $minMedianRestingHeartRate and $maxMedianRestingHeartRate",
                "Median heart rate ($median bpm) within range"
            )
        } else {
            EvaluationFactory.recoverableFail(
                "Patient does not have median heart rate between $minMedianRestingHeartRate and $maxMedianRestingHeartRate",
                "Median heart rate ($median bpm) outside range"
            )
        }
    }

    companion object {
        const val HEART_RATE_EXPECTED_UNIT: String = "BPM"
        private const val MAX_HEART_RATES_TO_USE = 5
    }
}