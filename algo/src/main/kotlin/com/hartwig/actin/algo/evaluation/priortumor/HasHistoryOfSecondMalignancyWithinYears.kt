package com.hartwig.actin.algo.evaluation.priortumor

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
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
            EvaluationFactory.pass("Has other malignancy in recent history")
        } else if (hasPotentialMatch) {
            EvaluationFactory.undetermined("Has history of previous malignancy but undetermined whether it is considered recent")
        } else {
            if (record.priorSecondPrimaries.isEmpty() || hasUsableData) {
                EvaluationFactory.fail("No recent other malignancy")
            } else {
                EvaluationFactory.undetermined("Undetermined if previous malignancy was recent (date unknown)")
            }
        }
    }
}