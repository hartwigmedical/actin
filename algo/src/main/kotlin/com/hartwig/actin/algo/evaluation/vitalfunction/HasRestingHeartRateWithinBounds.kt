package com.hartwig.actin.algo.evaluation.vitalfunction

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.clinical.datamodel.VitalFunctionCategory
import java.time.LocalDate

class HasRestingHeartRateWithinBounds(
    private val minMedianRestingHeartRate: Double, private val maxMedianRestingHeartRate: Double, private val minimalDate: LocalDate
) :
    EvaluationFunction {
    override fun evaluate(record: PatientRecord): Evaluation {
        val relevant =
            VitalFunctionSelector.selectMedianPerDay(record, VitalFunctionCategory.HEART_RATE, MAX_HEART_RATES_TO_USE, minimalDate)
        val wrongUnit = VitalFunctionSelector.selectRecentVitalFunctionsWrongUnit(record, VitalFunctionCategory.HEART_RATE)

        if (relevant.isEmpty()) {
            return EvaluationFactory.undetermined(
                if (wrongUnit.isEmpty()) "No (recent) heart rate data found" else "Heart rates not measured in $HEART_RATE_EXPECTED_UNIT"
            )
        }

        val median = VitalFunctionFunctions.determineMedianValue(relevant)
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