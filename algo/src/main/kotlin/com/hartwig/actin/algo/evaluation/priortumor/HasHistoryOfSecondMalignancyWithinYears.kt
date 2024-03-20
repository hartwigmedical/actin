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
        for (priorSecondPrimary in record.priorSecondPrimaries) {
            val (effectiveMinDate, secondPrimaryYear, secondPrimaryMonth) = if (priorSecondPrimary.lastTreatmentYear != null) {
                Triple(minDate, priorSecondPrimary.lastTreatmentYear, priorSecondPrimary.lastTreatmentMonth)
            } else {
                Triple(minDate.minusYears(1), priorSecondPrimary.diagnosedYear, priorSecondPrimary.diagnosedMonth)
            }
            if (secondPrimaryYear != null) {
                hasUsableData = true
                if (!LocalDate.of(secondPrimaryYear, secondPrimaryMonth ?: 1, 1).isBefore(effectiveMinDate)) {
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
                "Patient has history of recent previous malignancy with unclear dates"
            )
        } else {
            if (record.priorSecondPrimaries.isEmpty() || hasUsableData) {
                EvaluationFactory.fail(
                    "Patient has no history of recent previous malignancy", "No recent previous malignancy"
                )
            } else {
                EvaluationFactory.undetermined(
                    "Patient has previous malignancy, but no dates available so cannot be determined if previous malignancy was recent",
                    "Second primary history dates unknown"
                )
            }
        }
    }
}