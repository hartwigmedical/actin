package com.hartwig.actin.algo.evaluation.vitalfunction

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.clinical.datamodel.VitalFunctionCategory

class HasSufficientPulseOximetry internal constructor(private val minMedianPulseOximetry: Double) : EvaluationFunction {
    override fun evaluate(record: PatientRecord): Evaluation {
        val relevant = VitalFunctionSelector.selectMedianPerDay(record, VitalFunctionCategory.SPO2, MAX_PULSE_OXIMETRY_TO_USE)
        val wrongUnit = VitalFunctionSelector.selectRecentVitalFunctionsWrongUnit(record, VitalFunctionCategory.SPO2)

        if (relevant.isEmpty() && wrongUnit.isEmpty()) {
            return EvaluationFactory.recoverableUndetermined("No (recent) pulse oximetry data found")
        } else if (relevant.isEmpty()) {
            return EvaluationFactory.recoverableUndetermined("Pulse oximetry measurements not in correct unit (${EXPECTED_UNIT})")
        }

        val relevantPulseOximetries = VitalFunctionSelector.selectMedianPerDay(
            record, VitalFunctionCategory.SPO2, MAX_PULSE_OXIMETRY_TO_USE
        )
        val median = VitalFunctionFunctions.determineMedianValue(relevantPulseOximetries)
        return if (median.compareTo(minMedianPulseOximetry) >= 0) {
            EvaluationFactory.recoverablePass(
                "Patient has median pulse oximetry exceeding $minMedianPulseOximetry",
                "Pulse oximetry above $minMedianPulseOximetry"
            )
        } else {
            EvaluationFactory.recoverableFail(
                "Patient has median pulse oximetry ($median%) below $minMedianPulseOximetry",
                "Median pulse oximetry ($median%) below $minMedianPulseOximetry"
            )
        }
    }

    companion object {
        private const val EXPECTED_UNIT: String = "percent"
        private const val MAX_PULSE_OXIMETRY_TO_USE = 5
    }
}