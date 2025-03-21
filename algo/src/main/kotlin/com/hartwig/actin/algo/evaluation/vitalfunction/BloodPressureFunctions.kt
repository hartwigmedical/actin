package com.hartwig.actin.algo.evaluation.vitalfunction

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
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

        val median = VitalFunctionFunctions.determineMedianValue(relevant)
        val comparison = median.compareTo(referenceBloodPressure)

        return when {
            comparison < 0 -> {
                val message = "Median $categoryDisplay (${median.roundToInt()} mmHg) below $referenceBloodPressure mmHg"
                val marginOfErrorMsg = " but within margin of error"
                if (referenceIsMinimum) {
                    val referenceWithMargin = referenceBloodPressure * VitalFunctionRuleMapper.VITAL_FUNCTION_NEGATIVE_MARGIN_OF_ERROR
                    if (median >= referenceWithMargin) {
                        EvaluationFactory.recoverableUndetermined(message + marginOfErrorMsg)
                    } else {
                        EvaluationFactory.recoverableFail(message)
                    }
                } else {
                    EvaluationFactory.recoverablePass(message)
                }
            }

            comparison == 0 -> {
                return EvaluationFactory.recoverablePass(
                    "Median $categoryDisplay (${median.roundToInt()} mmHg) equal to $referenceBloodPressure mmHg"
                )
            }

            else -> {
                val message = "Median $categoryDisplay (${median.roundToInt()} mmHg) above $referenceBloodPressure mmHg"
                val marginOfErrorMsg = " but within margin of error"
                if (!referenceIsMinimum) {
                    val referenceWithMargin = referenceBloodPressure * VitalFunctionRuleMapper.VITAL_FUNCTION_POSITIVE_MARGIN_OF_ERROR
                    if (median <= referenceWithMargin) {
                        EvaluationFactory.recoverableUndetermined(message + marginOfErrorMsg)
                    } else {
                        EvaluationFactory.recoverableFail(message)
                    }
                } else {
                    EvaluationFactory.recoverablePass(message)
                }
            }
        }
    }
}