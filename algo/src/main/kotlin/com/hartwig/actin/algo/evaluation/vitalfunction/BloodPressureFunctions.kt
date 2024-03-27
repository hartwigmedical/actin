package com.hartwig.actin.algo.evaluation.vitalfunction

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import java.time.LocalDate
import kotlin.math.roundToInt

object BloodPressureFunctions {

    fun evaluatePatientMinimumBloodPressure(
        record: PatientRecord, category: BloodPressureCategory, minimalBloodPressure: Int, minimalDate: LocalDate
    ): Evaluation {
        return evaluatePatientBloodPressureAgainstReference(record, category, minimalBloodPressure, true, minimalDate)
    }

    fun evaluatePatientMaximumBloodPressure(
        record: PatientRecord, category: BloodPressureCategory, maximumBloodPressure: Int, minimalDate: LocalDate
    ): Evaluation {
        return evaluatePatientBloodPressureAgainstReference(record, category, maximumBloodPressure, false, minimalDate)
    }

    private fun evaluatePatientBloodPressureAgainstReference(
        record: PatientRecord,
        category: BloodPressureCategory,
        referenceBloodPressure: Int,
        referenceIsMinimum: Boolean,
        minimalDate: LocalDate
    ): Evaluation {
        val categoryDisplay = category.display().lowercase()
        val relevant = VitalFunctionSelector.selectBloodPressures(record, category, minimalDate)
        if (relevant.isEmpty()) return EvaluationFactory.recoverableUndetermined("No (recent) data found for $categoryDisplay")

        val referenceWithMargin = if (referenceIsMinimum) {
            referenceBloodPressure * VitalFunctionRuleMapper.VITAL_FUNCTION_NEGATIVE_MARGIN_OF_ERROR
        } else referenceBloodPressure * VitalFunctionRuleMapper.VITAL_FUNCTION_POSITIVE_MARGIN_OF_ERROR
        val median = VitalFunctionFunctions.determineMedianValue(relevant)
        val comparisonWithMargin = median.compareTo(referenceWithMargin)
        val comparisonWithoutMargin = median.compareTo(referenceBloodPressure)

        return when {
            (!referenceIsMinimum && comparisonWithoutMargin > 0 && comparisonWithMargin <= 0)
                    || (referenceIsMinimum && comparisonWithoutMargin < 0 && comparisonWithMargin >= 0) -> {
                val messageEnding = "$categoryDisplay (${median.roundToInt()} mmHg) below $referenceBloodPressure mmHg" +
                "but within margin of error"
                val specificMessage = "Patient has median $messageEnding"
                val generalMessage = "Median $messageEnding"
                EvaluationFactory.recoverableUndetermined(specificMessage, generalMessage)
            }

            comparisonWithoutMargin < 0 -> {
                val specificMessage = "Patient has median $categoryDisplay (${median.roundToInt()} mmHg) below $referenceBloodPressure mmHg"
                val generalMessage = "Median $categoryDisplay (${median.roundToInt()} mmHg) below $referenceBloodPressure mmHg"

                if (referenceIsMinimum) {
                    EvaluationFactory.recoverableFail(specificMessage, generalMessage)
                } else {
                    EvaluationFactory.recoverablePass(specificMessage, generalMessage)
                }
            }

            comparisonWithoutMargin == 0 -> {
                val specificMessage = "Patient has median $categoryDisplay (${median.roundToInt()} mmHg) " +
                        "equal to $referenceBloodPressure mmHg"
                val generalMessage = "Median $categoryDisplay (${median.roundToInt()} mmHg) equal to $referenceBloodPressure mmHg"

                return EvaluationFactory.recoverablePass(specificMessage, generalMessage)
            }

            else -> {
                val specificMessage = "Patient has median $categoryDisplay (${median.roundToInt()} mmHg) above $referenceBloodPressure mmHg"
                val generalMessage = "Median $categoryDisplay (${median.roundToInt()} mmHg) above $referenceBloodPressure mmHg"

                if (referenceIsMinimum) {
                    EvaluationFactory.recoverablePass(specificMessage, generalMessage)
                } else {
                    EvaluationFactory.recoverableFail(specificMessage, generalMessage)
                }
            }
        }
    }
}