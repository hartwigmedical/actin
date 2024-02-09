package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.util.DateComparison
import com.hartwig.actin.clinical.datamodel.treatment.history.TreatmentHistoryEntry
import java.time.LocalDate

class HasNotReceivedAnyCancerTreatmentSinceDate(
    private val minDate: LocalDate?,
    private val monthsAgo: Int
) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val priorCancerTreatment = record.clinical.oncologicalHistory

        return when {
            priorCancerTreatment.isEmpty() -> {
                EvaluationFactory.pass(
                    "Patient has not had any prior cancer treatments",
                    "Has not had any cancer treatment"
                )
            }

            priorCancerTreatment.none { treatmentSinceMinDate(it, false) } && priorCancerTreatment.any {
                treatmentSinceMinDate(
                    it,
                    true
                )
            } -> {
                EvaluationFactory.undetermined(
                    "Patient has had anti-cancer therapy (${priorCancerTreatment.joinToString(", ")}) but " +
                            "undetermined if in the last $monthsAgo months (date unknown)",
                    "Received anti-cancer therapy (${priorCancerTreatment.joinToString(", ")}) but " +
                            "undetermined if in the last $monthsAgo months (date unknown)"
                )
            }

            priorCancerTreatment.none { treatmentSinceMinDate(it, false) } -> {
                EvaluationFactory.pass(
                    "Patient has not received anti-cancer therapy within $monthsAgo months",
                    "Has not received anti-cancer therapy within $monthsAgo months"
                )
            }

            priorCancerTreatment.none { treatmentSinceMinDatePlusOneMonth(it) } -> {
                EvaluationFactory.warn(
                    "Patient has received anti-cancer therapy within $monthsAgo months (therapy ${monthsAgo.minus(1)} months ago)",
                    "Has received anti-cancer therapy within $monthsAgo months (therapy ${monthsAgo.minus(1)} months ago)"
                )
            }

            else -> {
                EvaluationFactory.fail(
                    "Patient has had anti-cancer therapy (${priorCancerTreatment.joinToString(", ")}) within the last $monthsAgo months",
                    "Received anti-cancer therapy (${priorCancerTreatment.joinToString(", ")}) within the last $monthsAgo months"
                )
            }
        }
    }

    private fun treatmentSinceMinDate(treatment: TreatmentHistoryEntry, includeUnknown: Boolean): Boolean {
        return DateComparison.isAfterDate(
            minDate!!,
            treatment.treatmentHistoryDetails?.stopYear,
            treatment.treatmentHistoryDetails?.stopMonth
        )
            ?: DateComparison.isAfterDate(minDate, treatment.startYear, treatment.startMonth)
            ?: includeUnknown
    }

    private fun treatmentSinceMinDatePlusOneMonth(treatment: TreatmentHistoryEntry): Boolean {
        val minDatePlusOneMonth = minDate!!.plusMonths(1)
        return DateComparison.isAfterDate(
            minDatePlusOneMonth,
            treatment.treatmentHistoryDetails?.stopYear,
            treatment.treatmentHistoryDetails?.stopMonth
        )
            ?: DateComparison.isAfterDate(minDatePlusOneMonth, treatment.startYear, treatment.startMonth)
            ?: false
    }
}
