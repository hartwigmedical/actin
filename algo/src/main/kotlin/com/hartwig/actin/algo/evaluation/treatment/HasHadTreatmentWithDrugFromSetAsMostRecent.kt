package com.hartwig.actin.algo.evaluation.treatment


import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.util.Format
import com.hartwig.actin.clinical.datamodel.treatment.Drug
import com.hartwig.actin.clinical.datamodel.treatment.Drug.Companion.UNKNOWN_PREFIX
import com.hartwig.actin.clinical.datamodel.treatment.DrugTreatment
import com.hartwig.actin.clinical.datamodel.treatment.Treatment
import com.hartwig.actin.clinical.datamodel.treatment.history.TreatmentHistoryEntry

class HasHadTreatmentWithDrugFromSetAsMostRecent(private val drugsToMatch: Set<Drug>) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val history = record.oncologicalHistory
        if (history.isEmpty()) {
            return EvaluationFactory.fail("No prior treatments in history")
        }

        val (historyWithoutDates, historyWithDates) = history.partition { it.startYear == null }
        val mostRecentTreatmentEntry = historyWithDates.maxWithOrNull(TreatmentHistoryEntryStartDateComparator())

        val drugNamesToMatch = drugsToMatch.map { drug -> drug.name.lowercase() }.toSet()
        val matchingDrugsInMostRecentLineWithDate = mostRecentTreatmentEntry?.let { selectMatchingDrugsFromEntry(it, drugNamesToMatch) }
            ?: emptyList()
        val matchingDrugsInUnknownTreatmentLines = historyWithoutDates.flatMap { selectMatchingDrugsFromEntry(it, drugNamesToMatch) }
            .toSet()

        val matchingDrugsInMostRecentLine = when {
            matchingDrugsInMostRecentLineWithDate.isNotEmpty() && historyWithoutDates.isEmpty() -> matchingDrugsInMostRecentLineWithDate
            matchingDrugsInUnknownTreatmentLines.isNotEmpty() && history.size == 1 -> matchingDrugsInUnknownTreatmentLines
            else -> emptyList()
        }

        val categoriesToMatch = drugsToMatch.map(Drug::category).toSet()
        val possibleTrialMatch = (if (history.size == 1) history.first() else mostRecentTreatmentEntry)?.let { mostRecent ->
            val mostRecentTreatments = mostRecent.allTreatments()
            val hasUnknownDrugWithCategory = drugsFromTreatments(mostRecentTreatments).any { drug ->
                drug.name.uppercase().startsWith(UNKNOWN_PREFIX) && drug.category in categoriesToMatch
            }
            mostRecent.isTrial && (mostRecentTreatments.isEmpty() || hasUnknownDrugWithCategory)
        } ?: false
        
        val drugsToMatchDisplay = "received ${Format.concatItemsWithOr(drugsToMatch)}"

        return when {
            matchingDrugsInMostRecentLine.isNotEmpty() -> {
                val matchingDrugDisplay = Format.concatItemsWithAnd(matchingDrugsInMostRecentLine)
                EvaluationFactory.pass(
                    "Patient has received $matchingDrugDisplay as most recent treatment",
                    "Has received $matchingDrugDisplay as most recent treatment"
                )
            }

            matchingDrugsInUnknownTreatmentLines.isNotEmpty() || matchingDrugsInMostRecentLineWithDate.isNotEmpty() -> {
                val drugList = Format.concatItemsWithAnd(matchingDrugsInUnknownTreatmentLines + matchingDrugsInMostRecentLineWithDate)
                val display = "Has received $drugList but undetermined if most recent"
                EvaluationFactory.undetermined("$display (dates missing in treatment list)", display)
            }

            possibleTrialMatch -> {
                EvaluationFactory.undetermined(
                    "Undetermined if patient has $drugsToMatchDisplay - exact drugs in recent trial unknown",
                    "Undetermined if $drugsToMatchDisplay - exact drugs in recent trial unknown"
                )
            }

            history.flatMap { selectMatchingDrugsFromEntry(it, drugNamesToMatch) }.isNotEmpty() -> {
                EvaluationFactory.fail(
                    "Patient has $drugsToMatchDisplay but not as the most recent treatment line",
                    "Has $drugsToMatchDisplay but not as most recent line"
                )
            }

            else -> {
                EvaluationFactory.fail(
                    "Patient has not $drugsToMatchDisplay",
                    "Has not $drugsToMatchDisplay"
                )
            }
        }
    }

    private fun drugsFromTreatments(treatments: Set<Treatment>) =
        treatments.flatMap { treatment -> (treatment as? DrugTreatment)?.drugs ?: emptyList() }

    private fun selectMatchingDrugsFromEntry(treatmentHistoryEntry: TreatmentHistoryEntry, drugNamesToMatch: Set<String>): Set<Drug> {
        return drugsFromTreatments(treatmentHistoryEntry.allTreatments()).filter { it.name.lowercase() in drugNamesToMatch }.toSet()
    }
}