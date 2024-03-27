package com.hartwig.actin.algo.evaluation.vitalfunction

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.vitalfunction.VitalFunctionRuleMapper.Companion.VITAL_FUNCTION_NEGATIVE_MARGIN_OF_ERROR
import com.hartwig.actin.clinical.datamodel.VitalFunctionCategory
import java.time.LocalDate

class HasSufficientPulseOximetry internal constructor(private val minMedianPulseOximetry: Double, private val minimumDate: LocalDate) :
    EvaluationFunction {
    override fun evaluate(record: PatientRecord): Evaluation {
        val relevant = VitalFunctionSelector.selectMedianPerDay(record, VitalFunctionCategory.SPO2, MAX_PULSE_OXIMETRY_TO_USE, minimumDate)
        val wrongUnit = VitalFunctionSelector.selectRecentVitalFunctionsWrongUnit(record, VitalFunctionCategory.SPO2)

        if (relevant.isEmpty()) {
            return EvaluationFactory.undetermined(
                if (wrongUnit.isEmpty()) {
                    "No (recent) pulse oximetry data found"
                } else {
                    "Pulse oximetry measurements not in correct unit (${EXPECTED_UNIT})"
                }
            )
        }

        val median = VitalFunctionFunctions.determineMedianValue(relevant)
        val referenceWithMargin = minMedianPulseOximetry * VITAL_FUNCTION_NEGATIVE_MARGIN_OF_ERROR

        return when {
            median.compareTo(minMedianPulseOximetry) >= 0 -> {
                EvaluationFactory.recoverablePass(
                    "Patient has median pulse oximetry exceeding $minMedianPulseOximetry",
                    "Pulse oximetry above $minMedianPulseOximetry"
                )
            }
            (median.compareTo(referenceWithMargin) >= 0) -> {
                EvaluationFactory.recoverableUndetermined(
                    "Patient has median pulse oximetry ($median%) below $minMedianPulseOximetry",
                    "Median pulse oximetry ($median%) below $minMedianPulseOximetry"
                )
            }
            else -> {
                EvaluationFactory.recoverableFail(
                    "Patient has median pulse oximetry ($median%) below $minMedianPulseOximetry",
                    "Median pulse oximetry ($median%) below $minMedianPulseOximetry"
                )
            }
        }
    }

    companion object {
        private const val EXPECTED_UNIT: String = "percent"
        private const val MAX_PULSE_OXIMETRY_TO_USE = 5
    }
}