package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.util.Format
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.treatment.Drug
import com.hartwig.actin.datamodel.clinical.treatment.Drug.Companion.UNKNOWN_PREFIX
import com.hartwig.actin.datamodel.clinical.treatment.DrugTreatment
import com.hartwig.actin.datamodel.clinical.treatment.Treatment
import com.hartwig.actin.datamodel.clinical.treatment.history.TreatmentHistoryEntry

class HasHadTreatmentWithDrugFromSetAsMostRecent(private val drugsToMatch: Set<Drug>, private val requireCurrentAdministration: Boolean) :
    EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val relevantHistory = record.oncologicalHistory.filter { entry ->
            entry.allTreatments().isEmpty() || entry.allTreatments().any { it is DrugTreatment }
        }
        val drugsToMatchDisplay = Format.concatItemsWithOr(drugsToMatch)
        if (relevantHistory.isEmpty()) {
            return EvaluationFactory.fail("No $drugsToMatchDisplay in provided treatments")
        }

        val (historyWithoutDates, historyWithDates) = relevantHistory.partition { it.startYear == null }
        val mostRecentTreatmentEntry = historyWithDates.maxWithOrNull(TreatmentHistoryEntryStartDateComparator())

        val drugNamesToMatch = drugsToMatch.map { drug -> drug.name.lowercase() }.toSet()
        val matchingDrugsInMostRecentLineWithDate =
            mostRecentTreatmentEntry?.let { selectMatchingDrugsFromEntry(it, drugNamesToMatch) } ?: emptyList()
        val matchingDrugsInUnknownTreatmentLines =
            historyWithoutDates.flatMap { selectMatchingDrugsFromEntry(it, drugNamesToMatch) }.toSet()

        val mostRecentMatchingEntry = when {
            matchingDrugsInMostRecentLineWithDate.isNotEmpty() && historyWithoutDates.isEmpty() -> mostRecentTreatmentEntry
            matchingDrugsInUnknownTreatmentLines.isNotEmpty() && relevantHistory.size == 1 -> historyWithoutDates.firstOrNull()
            else -> null
        }
        val mostRecentMatchingEntryHasStopDate = mostRecentMatchingEntry?.treatmentHistoryDetails?.stopYear != null
        val matchingDrugsInMostRecentLine = mostRecentMatchingEntry?.let {
            selectMatchingDrugsFromEntry(it, drugNamesToMatch)
        } ?: emptySet()

        return when {
            matchingDrugsInMostRecentLine.isNotEmpty() -> {
                val matchingDrugDisplay = Format.concatItemsWithAnd(matchingDrugsInMostRecentLine)
                when (requireCurrentAdministration) {
                    false -> EvaluationFactory.pass("$matchingDrugDisplay in provided treatments as most recent treatment")
                    true -> {
                        if (mostRecentMatchingEntryHasStopDate) {
                            EvaluationFactory.fail("Does not currently receive $matchingDrugDisplay (treatment has stopped)")
                        } else {
                            EvaluationFactory.undetermined("$matchingDrugDisplay in provided treatments as most recent treatment but unknown if currently still administered")
                        }
                    }
                }
            }

            matchingDrugsInUnknownTreatmentLines.isNotEmpty() || matchingDrugsInMostRecentLineWithDate.isNotEmpty() -> {
                val drugList = Format.concatItemsWithAnd(matchingDrugsInUnknownTreatmentLines + matchingDrugsInMostRecentLineWithDate)
                val display = "$drugList in provided treatments but undetermined if most recent"
                val currentlyDisplay = if (requireCurrentAdministration) " and unknown if currently still administered" else ""
                EvaluationFactory.undetermined("$display (date unknown)$currentlyDisplay")
            }

            possibleTrialMatch(if (relevantHistory.size == 1) relevantHistory.first() else mostRecentTreatmentEntry) -> {
                val currentlyDisplay = if (requireCurrentAdministration) " and unknown if currently still administered" else ""
                EvaluationFactory.undetermined(
                    "Undetermined if treatment from previous trial included " +
                            "${Format.concatItemsWithOr(drugsToMatch)}$currentlyDisplay"
                )
            }

            relevantHistory.flatMap { selectMatchingDrugsFromEntry(it, drugNamesToMatch) }.isNotEmpty() -> {
                val currentlyDisplay = if (requireCurrentAdministration) " and hence not currently administered" else ""
                EvaluationFactory.fail("${Format.concatItemsWithOr(drugsToMatch)} in provided treatments but not as most recent line$currentlyDisplay")
            }

            else -> EvaluationFactory.fail("No $drugsToMatchDisplay in provided treatments")
        }
    }

    private fun possibleTrialMatch(mostRecentTreatmentEntry: TreatmentHistoryEntry?): Boolean {
        return mostRecentTreatmentEntry?.let { mostRecent ->
            val mostRecentTreatments = mostRecent.allTreatments()
            val categoriesToMatch = drugsToMatch.map(Drug::category).toSet()
            val hasUnknownDrugWithCategory = drugsFromTreatments(mostRecentTreatments).any { drug ->
                drug.name.uppercase().startsWith(UNKNOWN_PREFIX) && drug.category in categoriesToMatch
            }
            mostRecent.isTrial && (mostRecentTreatments.isEmpty() || hasUnknownDrugWithCategory)
        } ?: false
    }

    private fun drugsFromTreatments(treatments: Set<Treatment>) =
        treatments.flatMap { treatment -> (treatment as? DrugTreatment)?.drugs ?: emptyList() }

    private fun selectMatchingDrugsFromEntry(treatmentHistoryEntry: TreatmentHistoryEntry, drugNamesToMatch: Set<String>): Set<Drug> {
        return drugsFromTreatments(treatmentHistoryEntry.allTreatments()).filter { it.name.lowercase() in drugNamesToMatch }.toSet()
    }
}