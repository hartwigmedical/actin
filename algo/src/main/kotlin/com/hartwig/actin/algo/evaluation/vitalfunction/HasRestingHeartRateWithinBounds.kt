package com.hartwig.actin.algo.evaluation.vitalfunction

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.VitalFunctionCategory
import java.time.LocalDate

class HasRestingHeartRateWithinBounds(
    private val minMedianRestingHeartRate: Double, private val maxMedianRestingHeartRate: Double, private val minimumDate: LocalDate
) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val relevant =
            VitalFunctionSelector.selectMedianPerDay(record, VitalFunctionCategory.HEART_RATE, MAX_HEART_RATES_TO_USE, minimumDate)
        val wrongUnit = VitalFunctionSelector.selectRecentVitalFunctionsWrongUnit(record, VitalFunctionCategory.HEART_RATE)

        if (relevant.isEmpty()) {
            return EvaluationFactory.undetermined(
                if (wrongUnit.isEmpty()) "No (recent) heart rate data found" else "Heart rates not measured in $HEART_RATE_EXPECTED_UNIT"
            )
        }

        val median = VitalFunctionFunctions.determineMedianValue(relevant)
        val minHeartRateWithMargin = minMedianRestingHeartRate * VitalFunctionRuleMapper.VITAL_FUNCTION_NEGATIVE_MARGIN_OF_ERROR
        val maxHeartRateWithMargin = maxMedianRestingHeartRate * VitalFunctionRuleMapper.VITAL_FUNCTION_POSITIVE_MARGIN_OF_ERROR

        return when (median) {
            in minMedianRestingHeartRate..maxMedianRestingHeartRate -> {
                EvaluationFactory.recoverablePass(
                    "Median heart rate ($median bpm) within range ($minMedianRestingHeartRate - $maxMedianRestingHeartRate)"
                )
            }

            in minHeartRateWithMargin..maxHeartRateWithMargin -> {
                EvaluationFactory.recoverableUndetermined(
                    "Median heart rate ($median bpm) outside range ($minMedianRestingHeartRate - $maxMedianRestingHeartRate) " +
                            "but within margin of error"
                )
            }

            else -> {
                EvaluationFactory.recoverableFail(
                    "Median heart rate ($median bpm) outside range ($minMedianRestingHeartRate - $maxMedianRestingHeartRate)"
                )
            }
        }
    }

    companion object {
        const val HEART_RATE_EXPECTED_UNIT: String = "BPM"
        private const val MAX_HEART_RATES_TO_USE = 5
    }
}