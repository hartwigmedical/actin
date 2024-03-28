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
    private val intents: Set<Intent>?,
    private val minDate: LocalDate?,
    private val monthsAgo: Int?
) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {

        val systemicTreatments = record.oncologicalHistory.filter { it.allTreatments().any(Treatment::isSystemic) }
        val matchingTreatments = intents?.let { intents ->
            systemicTreatments.groupBy { it.intents?.any { intent -> intent in intents } }
        } ?: systemicTreatments.groupBy { true }

        val intentsLowercase = intents?.let { concatItemsWithOr(it).lowercase() }

        return when {
            monthsAgo == null && matchingTreatments.containsKey(true) -> {
                EvaluationFactory.pass("Patient has had $intentsLowercase systemic therapy", "Received $intentsLowercase systemic therapy")
            }

            matchingTreatments[true]?.any { treatmentSinceMinDate(it, false) } ?: false -> {
                EvaluationFactory.pass(
                    "Patient has had $intentsLowercase systemic therapy within the last $monthsAgo months",
                    "Received $intentsLowercase systemic therapy within the last $monthsAgo months"
                )
            }

            matchingTreatments[true]?.any { treatmentSinceMinDate(it, true) } ?: false -> {
                EvaluationFactory.undetermined(
                    "Patient has had $intentsLowercase systemic therapy but date unknown",
                    "Received $intentsLowercase systemic therapy but date unknown"
                )
            }

            (monthsAgo == null && matchingTreatments.containsKey(key = null)) || matchingTreatments[null]?.any {
                treatmentSinceMinDate(
                    it,
                    true
                )
            } ?: false -> {
                EvaluationFactory.undetermined("Undetermined if intent of received systemic treatment is $intentsLowercase")
            }

            !matchingTreatments.containsKey(true) -> {
                EvaluationFactory.fail(
                    "Patient has not had any $intentsLowercase systemic therapy in prior tumor history",
                    "No $intentsLowercase systemic therapy in prior tumor history"
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
        return isAfterDate(minDate!!, treatment.treatmentHistoryDetails?.stopYear, treatment.treatmentHistoryDetails?.stopMonth)
            ?: isAfterDate(minDate, treatment.startYear, treatment.startMonth)
            ?: includeUnknown
    }
}
