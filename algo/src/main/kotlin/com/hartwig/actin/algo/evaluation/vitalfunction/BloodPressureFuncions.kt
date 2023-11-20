package com.hartwig.actin.algo.evaluation.vitalfunction

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import kotlin.math.roundToInt

object BloodPressureFuncions {

    fun evaluatePatientBloodPressureAgainstMin(record: PatientRecord, category: BloodPressureCategory, referenceBloodPressure: Int
    ): Evaluation {
        return evaluatePatientBloodPressureAgainstReference(record, category, referenceBloodPressure, true)
    }

    fun evaluatePatientBloodPressureAgainstMax(record: PatientRecord, category: BloodPressureCategory, referenceBloodPressure: Int
    ): Evaluation {
        return evaluatePatientBloodPressureAgainstReference(record, category, referenceBloodPressure, false)
    }

    private fun evaluatePatientBloodPressureAgainstReference(
        record: PatientRecord, category: BloodPressureCategory, referenceBloodPressure: Int, referenceIsMinimum: Boolean
    ): Evaluation {

        val categoryDisplay = category.display().lowercase()
        val relevant = VitalFunctionSelector.selectBloodPressures(record.clinical().vitalFunctions(), category)
        if (relevant.isEmpty()) return EvaluationFactory.undetermined("No data found for $categoryDisplay")
        val median = VitalFunctionFunctions.determineMedianValue(relevant)
        val comparison = median.compareTo(referenceBloodPressure)

        return when {
            comparison < 0 -> {
                val specificMessage = "Patient has median $categoryDisplay (${median.roundToInt()} mmHg) below $referenceBloodPressure mmHg"
                val generalMessage = "Median $categoryDisplay (${median.roundToInt()} mmHg) below $referenceBloodPressure mmHg"

                if (referenceIsMinimum) {
                    EvaluationFactory.recoverableFail(specificMessage, generalMessage)
                } else {
                    EvaluationFactory.pass(specificMessage, generalMessage)
                }
            }

            comparison == 0 -> {
                val specificMessage = "Patient has median $categoryDisplay (${median.roundToInt()} mmHg) " +
                        "equal to $referenceBloodPressure mmHg"
                val generalMessage = "Median $categoryDisplay (${median.roundToInt()} mmHg) equal to $referenceBloodPressure mmHg"

                return EvaluationFactory.pass(specificMessage, generalMessage)
            }

            else -> {
                val specificMessage = "Patient has median $categoryDisplay (${median.roundToInt()} mmHg) above $referenceBloodPressure mmHg"
                val generalMessage = "Median $categoryDisplay (${median.roundToInt()} mmHg) above $referenceBloodPressure mmHg"

                if (referenceIsMinimum) {
                    EvaluationFactory.pass(specificMessage, generalMessage)
                } else {
                    EvaluationFactory.recoverableFail(specificMessage, generalMessage)
                }
            }
        }
    }
}