package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.treatment.TreatmentVersusDateFunctions.treatmentBeforeMaxDate
import com.hartwig.actin.algo.evaluation.treatment.TreatmentVersusDateFunctions.treatmentSinceMinDate
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
    private val refDate: LocalDate?,
    private val weeks: Int?,
    private val evaluateWithinWeeks: Boolean?
) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val systemicTreatments = record.oncologicalHistory.filter { it.allTreatments().any(Treatment::isSystemic) }
        val matchingTreatments = intents?.let { intents ->
            systemicTreatments.groupBy { it.intents?.any { intent -> intent in intents } }
        } ?: systemicTreatments.groupBy { true }

        val intentsLowercase = intents?.let { concatItemsWithOr(it).lowercase() } ?: ""

        return when {
            refDate == null && matchingTreatments.containsKey(true) -> {
                EvaluationFactory.pass("Received $intentsLowercase systemic therapy")
            }

            evaluateWithinWeeks == true && evaluateTreatments(matchingTreatments, ::treatmentSinceMinDate, false) -> {
                EvaluationFactory.pass("Received $intentsLowercase systemic therapy within the last $weeks weeks")
            }

            evaluateWithinWeeks == false && evaluateTreatments(matchingTreatments, ::treatmentBeforeMaxDate, false) -> {
                EvaluationFactory.pass("Received $intentsLowercase systemic therapy at least $weeks weeks ago")
            }

            (evaluateWithinWeeks == true && evaluateTreatments(matchingTreatments, ::treatmentSinceMinDate, true)) ||
                    (evaluateWithinWeeks == false && evaluateTreatments(matchingTreatments, ::treatmentBeforeMaxDate, true)) -> {
                EvaluationFactory.undetermined("Received $intentsLowercase systemic therapy but date unknown")
            }

            (evaluateWithinWeeks != false && matchingTreatments[null]?.let(::anyTreatmentPotentiallySinceMinDate) == true) ||
                    (evaluateWithinWeeks != true && matchingTreatments[null]?.let(::anyTreatmentPotentiallyBeforeMaxDate) == true) -> {
                EvaluationFactory.undetermined(
                    "Has received systemic treatment (${Format.concat(systemicTreatments.map { it.treatmentDisplay() })}) " +
                            "but undetermined if intent is $intentsLowercase"
                )
            }

            !matchingTreatments.containsKey(true) -> {
                EvaluationFactory.fail("No $intentsLowercase systemic therapy in prior tumor history")
            }

            evaluateWithinWeeks == true -> EvaluationFactory.fail("All $intentsLowercase systemic therapy is administered more than $weeks weeks ago")

            else -> EvaluationFactory.fail("All $intentsLowercase systemic therapy is not administered at least $weeks weeks ago")
        }
    }

    private fun anyTreatmentPotentiallySinceMinDate(treatmentEntries: Iterable<TreatmentHistoryEntry>): Boolean {
        return refDate == null || treatmentEntries.any { treatmentSinceMinDate(it, refDate, true) }
    }

    private fun anyTreatmentPotentiallyBeforeMaxDate(treatmentEntries: Iterable<TreatmentHistoryEntry>): Boolean {
        return refDate == null || treatmentEntries.any { treatmentBeforeMaxDate(it, refDate, true) }
    }

    private fun evaluateTreatments(
        matchingTreatments: Map<out Boolean?, List<TreatmentHistoryEntry>>,
        treatmentFunction: (TreatmentHistoryEntry, LocalDate, Boolean) -> Boolean,
        includeUnknown: Boolean
    ): Boolean {
        return refDate?.let { matchingTreatments[true]?.any { treatmentFunction(it, refDate, includeUnknown) } } == true
    }
}
