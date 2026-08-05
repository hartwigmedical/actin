package com.hartwig.actin.algo.evaluation.vitalfunction

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationLabels
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import java.time.LocalDate
import kotlin.math.roundToInt

object BloodPressureFunctions {

    fun evaluatePatientMinimumBloodPressure(
        record: PatientRecord, category: BloodPressureCategory, minimalBloodPressure: Int, minimalDate: LocalDate,
        labels: EvaluationLabels.VitalFunction
    ): Evaluation {
        return evaluatePatientBloodPressureAgainstReference(record, category, minimalBloodPressure, true, minimalDate, labels)
    }

    fun evaluatePatientMaximumBloodPressure(
        record: PatientRecord, category: BloodPressureCategory, maximumBloodPressure: Int, minimalDate: LocalDate,
        labels: EvaluationLabels.VitalFunction
    ): Evaluation {
        return evaluatePatientBloodPressureAgainstReference(record, category, maximumBloodPressure, false, minimalDate, labels)
    }

    private fun evaluatePatientBloodPressureAgainstReference(
        record: PatientRecord,
        category: BloodPressureCategory,
        referenceBloodPressure: Int,
        referenceIsMinimum: Boolean,
        minimalDate: LocalDate,
        labels: EvaluationLabels.VitalFunction
    ): Evaluation {
        val categoryDisplay = category.display().lowercase()
        val relevant = VitalFunctionSelector.selectBloodPressures(record, category, minimalDate)
        if (relevant.isEmpty()) return EvaluationFactory.recoverableUndetermined(labels.bloodPressureFunctionsNoData(categoryDisplay))

        val median = VitalFunctionFunctions.determineMedianValue(relevant)
        val comparison = median.compareTo(referenceBloodPressure)

        return when {
            comparison < 0 -> {
                val message = labels.bloodPressureFunctionsBelowReference(categoryDisplay, median.roundToInt(), referenceBloodPressure)
                val marginOfErrorMsg = labels.bloodPressureFunctionsMarginOfErrorSuffix()
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
                EvaluationFactory.recoverablePass(
                    labels.bloodPressureFunctionsEqualToReference(categoryDisplay, median.roundToInt(), referenceBloodPressure)
                )
            }

            else -> {
                val message = labels.bloodPressureFunctionsAboveReference(categoryDisplay, median.roundToInt(), referenceBloodPressure)
                val marginOfErrorMsg = labels.bloodPressureFunctionsMarginOfErrorSuffix()
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