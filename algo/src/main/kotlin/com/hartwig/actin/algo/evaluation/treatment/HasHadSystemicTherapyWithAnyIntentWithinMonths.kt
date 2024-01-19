package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.util.DateComparison
import com.hartwig.actin.algo.evaluation.util.Format.concatItemsWithOr
import com.hartwig.actin.clinical.datamodel.treatment.Treatment
import com.hartwig.actin.clinical.datamodel.treatment.history.Intent
import com.hartwig.actin.clinical.datamodel.treatment.history.TreatmentHistoryEntry
import java.time.LocalDate

class HasHadSystemicTherapyWithAnyIntentWithinMonths(
    private val intents: Set<Intent>,
    private val minDate: LocalDate,
    private val monthsAgo: Int
) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val matchingTreatments = record.clinical().oncologicalHistory()
            .filter { it.allTreatments().any(Treatment::isSystemic) }
            .filter { matchingIntent(it) }

        val intentsLowercase = concatItemsWithOr(intents).lowercase()

        return when {
            matchingTreatments.any { treatmentSinceMinDate(it, false) } -> {
                EvaluationFactory.pass(
                    "Patient has had $intentsLowercase systemic therapy within the last $monthsAgo months",
                    "Received $intentsLowercase systemic therapy within the last $monthsAgo months"
                )
            }

            matchingTreatments.any { treatmentSinceMinDate(it, true) } -> {
                EvaluationFactory.undetermined(
                    "Patient has had $intentsLowercase systemic therapy but date unknown",
                    "Received $intentsLowercase systemic therapy but date unknown"
                )
            }

            matchingTreatments.isNotEmpty() ->
                EvaluationFactory.fail(
                    "All $intentsLowercase systemic therapy is administered more than $monthsAgo months ago",
                    "No $intentsLowercase systemic therapy within $monthsAgo months"
                )

            else -> {
                EvaluationFactory.fail(
                    "Patient has not had any $intentsLowercase systemic therapy in prior tumor history",
                    "No $intentsLowercase systemic therapy in prior tumor history"
                )
            }
        }

    }

    private fun treatmentSinceMinDate(treatment: TreatmentHistoryEntry, includeUnknown: Boolean): Boolean {
        return DateComparison.isAfterDate(
            minDate,
            treatment.treatmentHistoryDetails()?.stopYear(),
            treatment.treatmentHistoryDetails()?.stopMonth()
        )
            ?: DateComparison.isAfterDate(minDate, treatment.startYear(), treatment.startMonth())
            ?: includeUnknown
    }

    private fun matchingIntent(treatment: TreatmentHistoryEntry): Boolean {
        for (intent in intents) {
            if (treatment.intents()?.any { it == intent } == true) {
                return true
            }
        }
        return false
    }
}
