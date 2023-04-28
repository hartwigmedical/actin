package com.hartwig.actin.algo.evaluation.priortumor

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import java.time.LocalDate

class HasHistoryOfSecondMalignancyWithinYears(private val minDate: LocalDate) : EvaluationFunction {
    override fun evaluate(record: PatientRecord): Evaluation {
        var hasMatch = false
        var hasPotentialMatch = false
        var hasUsableData = false
        for (priorSecondPrimary in record.clinical().priorSecondPrimaries()) {
            var effectiveMinDate = minDate.minusYears(1)
            var secondPrimaryYear = priorSecondPrimary.diagnosedYear()
            var secondPrimaryMonth = priorSecondPrimary.diagnosedMonth()
            if (priorSecondPrimary.lastTreatmentYear() != null) {
                effectiveMinDate = minDate
                secondPrimaryYear = priorSecondPrimary.lastTreatmentYear()
                secondPrimaryMonth = if (priorSecondPrimary.lastTreatmentMonth() != null) {
                    priorSecondPrimary.lastTreatmentMonth()
                } else {
                    null
                }
            }
            if (secondPrimaryYear != null) {
                hasUsableData = true
                val secondPrimaryDate = LocalDate.of(secondPrimaryYear, secondPrimaryMonth ?: 1, 1)
                if (!secondPrimaryDate.isBefore(effectiveMinDate)) {
                    hasMatch = true
                } else if (secondPrimaryYear == effectiveMinDate.year && secondPrimaryMonth == null) {
                    hasPotentialMatch = true
                }
            }
        }
        return if (hasMatch) {
            EvaluationFactory.pass(
                "Patient has history of recent previous malignancy", "Patient has history of recent previous malignancy"
            )
        } else if (hasPotentialMatch) {
            EvaluationFactory.undetermined(
                "Patient has history of previous malignancy but unclear whether it is recent enough",
                "Patient has history of recent previous malignancy, unclear dates"
            )
        } else {
            if (record.clinical().priorSecondPrimaries().isEmpty() || hasUsableData) {
                EvaluationFactory.fail(
                    "Patient has no history of recent previous malignancy", "No recent previous malignancy"
                )
            } else {
                EvaluationFactory.undetermined(
                    "Patient has previous malignancy, but no dates available so cannot be determined if previous malignancy was recent",
                    "Second primary history, dates unknown"
                )
            }
        }
    }
}