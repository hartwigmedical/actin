package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.treatment.TreatmentSinceDateFunctions.treatmentSinceMinDate
import com.hartwig.actin.algo.evaluation.util.Format
import com.hartwig.actin.algo.evaluation.util.Format.concatItemsWithOr
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.treatment.Treatment
import com.hartwig.actin.datamodel.clinical.treatment.history.Intent
import com.hartwig.actin.datamodel.clinical.treatment.history.TreatmentHistoryEntry
import java.time.LocalDate

class HasHadSystemicTherapyWithAnyIntent(
    private val intents: Set<Intent>?,
    private val minDate: LocalDate?,
    private val weeksAgo: Int?
) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {

        val systemicTreatments = record.oncologicalHistory.filter { it.allTreatments().any(Treatment::isSystemic) }
        val matchingTreatments = intents?.let { intents ->
            systemicTreatments.groupBy { it.intents?.any { intent -> intent in intents } }
        } ?: systemicTreatments.groupBy { true }

        val intentsLowercase = intents?.let { concatItemsWithOr(it).lowercase() } ?: ""

        return when {
            minDate == null && matchingTreatments.containsKey(true) -> {
                EvaluationFactory.pass("Patient has had $intentsLowercase systemic therapy", "Received $intentsLowercase systemic therapy")
            }

            minDate?.let { matchingTreatments[true]?.any { treatmentSinceMinDate(it, minDate, false) } } == true -> {
                EvaluationFactory.pass(
                    "Patient has had $intentsLowercase systemic therapy within the last $weeksAgo weeks",
                    "Received $intentsLowercase systemic therapy within the last $weeksAgo weeks"
                )
            }

            minDate?.let { matchingTreatments[true]?.any { treatmentSinceMinDate(it, minDate, true) } } == true -> {
                EvaluationFactory.undetermined(
                    "Patient has had $intentsLowercase systemic therapy but date unknown",
                    "Received $intentsLowercase systemic therapy but date unknown"
                )
            }

            matchingTreatments[null]?.let(::anyTreatmentPotentiallySinceMinDate) == true -> {
                EvaluationFactory.undetermined(
                    "Has received systemic treatment (${Format.concatWithCommaAndAnd(systemicTreatments.map { it.treatmentName() })}) " +
                            "but undetermined if intent is $intentsLowercase"
                )
            }

            !matchingTreatments.containsKey(true) -> {
                EvaluationFactory.fail(
                    "Patient has not had any $intentsLowercase systemic therapy in prior tumor history",
                    "No $intentsLowercase systemic therapy in prior tumor history"
                )
            }

            else ->
                EvaluationFactory.fail(
                    "All $intentsLowercase systemic therapy is administered more than $weeksAgo weeks ago",
                    "No $intentsLowercase systemic therapy within $weeksAgo weeks"
                )
        }
    }

    private fun anyTreatmentPotentiallySinceMinDate(treatmentEntries: Iterable<TreatmentHistoryEntry>): Boolean {
        return minDate == null || treatmentEntries.any { treatmentSinceMinDate(it, minDate, true) }
    }
}
