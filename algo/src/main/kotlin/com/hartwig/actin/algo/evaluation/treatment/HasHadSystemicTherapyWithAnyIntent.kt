package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.util.DateComparison.isAfterDate
import com.hartwig.actin.algo.evaluation.util.Format.concatItemsWithOr
import com.hartwig.actin.clinical.datamodel.treatment.Treatment
import com.hartwig.actin.clinical.datamodel.treatment.history.Intent
import com.hartwig.actin.clinical.datamodel.treatment.history.TreatmentHistoryEntry
import java.time.LocalDate

class HasHadSystemicTherapyWithAnyIntent(
    private val intents: Set<Intent>,
    private val minDate: LocalDate?,
    private val monthsAgo: Int?
) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val matchingTreatments = record.clinical().oncologicalHistory()
            .filter { it.allTreatments().any(Treatment::isSystemic) }
            .filter { it.intents()?.any { intent -> intent in intents } ?: false }

        val intentsLowercase = concatItemsWithOr(intents).lowercase()

        return when {
            (monthsAgo == null) && matchingTreatments.isNotEmpty() -> {
                EvaluationFactory.pass("Patient has had $intentsLowercase systemic therapy", "Received $intentsLowercase systemic therapy")
            }

            matchingTreatments.isEmpty() -> {
                EvaluationFactory.fail(
                    "Patient has not had any $intentsLowercase systemic therapy in prior tumor history",
                    "No $intentsLowercase systemic therapy in prior tumor history"
                )
            }

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

            else ->
                EvaluationFactory.fail(
                    "All $intentsLowercase systemic therapy is administered more than $monthsAgo months ago",
                    "No $intentsLowercase systemic therapy within $monthsAgo months"
                )
        }

    }

    private fun treatmentSinceMinDate(treatment: TreatmentHistoryEntry, includeUnknown: Boolean): Boolean {
        return isAfterDate(minDate!!, treatment.treatmentHistoryDetails()?.stopYear(), treatment.treatmentHistoryDetails()?.stopMonth())
            ?: isAfterDate(minDate, treatment.startYear(), treatment.startMonth())
            ?: includeUnknown
    }
}
