package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.util.DateComparison
import com.hartwig.actin.clinical.datamodel.treatment.history.TreatmentHistoryEntry
import java.time.LocalDate

class HasNotReceivedAnyCancerTreatmentSinceDate(
    private val minDate: LocalDate,
    private val monthsAgo: Int
) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val priorCancerTreatment = record.oncologicalHistory
        val concatenatedTreatmentDisplay = priorCancerTreatment.filter { treatmentSinceMinDate(it, minDate, true) }
            .joinToString { it.treatmentDisplay() }

        return when {
            priorCancerTreatment.isEmpty() -> {
                EvaluationFactory.pass(
                    "Patient has not had any prior cancer treatments",
                    "Has not had any cancer treatment"
                )
            }

            priorCancerTreatment.none { treatmentSinceMinDate(it, minDate, false) } && priorCancerTreatment.any {
                treatmentSinceMinDate(it, minDate, true)
            } -> {
                EvaluationFactory.undetermined(
                    "Patient has had anti-cancer therapy ($concatenatedTreatmentDisplay) but " +
                            "undetermined if in the last $monthsAgo months (date unknown)",
                    "Received anti-cancer therapy ($concatenatedTreatmentDisplay) but " +
                            "undetermined if in the last $monthsAgo months (date unknown)"
                )
            }

            priorCancerTreatment.none { treatmentSinceMinDate(it, minDate, false) } -> {
                EvaluationFactory.pass(
                    "Patient has not received anti-cancer therapy within $monthsAgo months",
                    "Has not received anti-cancer therapy within $monthsAgo months"
                )
            }

            priorCancerTreatment.none { treatmentSinceMinDate(it, minDate.plusMonths(1), false) } -> {
                EvaluationFactory.warn(
                    "Patient has received anti-cancer therapy within $monthsAgo months (therapy ${monthsAgo.minus(1)} months ago)",
                    "Has received anti-cancer therapy within $monthsAgo months (therapy ${monthsAgo.minus(1)} months ago)"
                )
            }

            else -> {
                EvaluationFactory.fail(
                    "Patient has had anti-cancer therapy ($concatenatedTreatmentDisplay) within the last $monthsAgo months",
                    "Received anti-cancer therapy ($concatenatedTreatmentDisplay) within the last $monthsAgo months"
                )
            }
        }
    }

    private fun treatmentSinceMinDate(treatment: TreatmentHistoryEntry, minDate: LocalDate, includeUnknown: Boolean): Boolean {
        return DateComparison.isAfterDate(
            minDate,
            treatment.treatmentHistoryDetails?.stopYear,
            treatment.treatmentHistoryDetails?.stopMonth
        )
            ?: DateComparison.isAfterDate(minDate, treatment.startYear, treatment.startMonth)
            ?: includeUnknown
    }
}
