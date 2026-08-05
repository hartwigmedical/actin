package com.hartwig.actin.algo.evaluation.priortumor

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.EvaluationLabels
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import java.time.LocalDate

class HasHistoryOfSecondMalignancyWithinYears(private val minDate: LocalDate, private val labels: EvaluationLabels.PriorTumor) :
    EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        var hasMatch = false
        var hasPotentialMatch = false
        var hasUsableData = false
        for (priorPrimary in record.priorPrimaries) {
            val (effectiveMinDate, secondPrimaryYear, secondPrimaryMonth) = if (priorPrimary.lastTreatmentYear != null) {
                Triple(minDate, priorPrimary.lastTreatmentYear, priorPrimary.lastTreatmentMonth)
            } else {
                Triple(minDate.minusYears(1), priorPrimary.diagnosedYear, priorPrimary.diagnosedMonth)
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

        return when {
            hasMatch -> EvaluationFactory.pass(labels.hasHistoryOfSecondMalignancyWithinYearsPass())
            hasPotentialMatch -> EvaluationFactory.undetermined(labels.hasHistoryOfSecondMalignancyWithinYearsUndeterminedPotentialMatch())
            record.priorPrimaries.isEmpty() || hasUsableData -> EvaluationFactory.fail(labels.hasHistoryOfSecondMalignancyWithinYearsFail())
            else -> EvaluationFactory.undetermined(labels.hasHistoryOfSecondMalignancyWithinYearsUndeterminedUnknownDate())
        }
    }
}