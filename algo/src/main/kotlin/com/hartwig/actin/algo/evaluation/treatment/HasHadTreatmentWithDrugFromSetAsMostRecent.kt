package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.EvaluationLabels
import com.hartwig.actin.algo.evaluation.util.Format
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.treatment.Drug
import com.hartwig.actin.datamodel.clinical.treatment.Drug.Companion.UNKNOWN_PREFIX
import com.hartwig.actin.datamodel.clinical.treatment.DrugTreatment
import com.hartwig.actin.datamodel.clinical.treatment.Treatment
import com.hartwig.actin.datamodel.clinical.treatment.history.TreatmentHistoryEntry

class HasHadTreatmentWithDrugFromSetAsMostRecent(
    private val drugsToMatch: Set<Drug>,
    private val labels: EvaluationLabels.Treatment
) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val history = record.oncologicalHistory
        if (history.isEmpty()) {
            return EvaluationFactory.fail(labels.hasHadTreatmentWithDrugFromSetAsMostRecentFailNoHistory())
        }

        val (historyWithoutDates, historyWithDates) = history.partition { it.startYear == null }
        val mostRecentTreatmentEntry = historyWithDates.maxWithOrNull(TreatmentHistoryEntryStartDateComparator())

        val drugNamesToMatch = drugsToMatch.map { drug -> drug.name.lowercase() }.toSet()
        val matchingDrugsInMostRecentLineWithDate =
            mostRecentTreatmentEntry?.let { selectMatchingDrugsFromEntry(it, drugNamesToMatch) } ?: emptyList()
        val matchingDrugsInUnknownTreatmentLines =
            historyWithoutDates.flatMap { selectMatchingDrugsFromEntry(it, drugNamesToMatch) }.toSet()

        val matchingDrugsInMostRecentLine = when {
            matchingDrugsInMostRecentLineWithDate.isNotEmpty() && historyWithoutDates.isEmpty() -> matchingDrugsInMostRecentLineWithDate
            matchingDrugsInUnknownTreatmentLines.isNotEmpty() && history.size == 1 -> matchingDrugsInUnknownTreatmentLines
            else -> emptyList()
        }

        return when {
            matchingDrugsInMostRecentLine.isNotEmpty() -> {
                val matchingDrugDisplay = Format.concatItemsWithAnd(matchingDrugsInMostRecentLine)
                EvaluationFactory.pass(labels.hasHadTreatmentWithDrugFromSetAsMostRecentPass(matchingDrugDisplay))
            }

            matchingDrugsInUnknownTreatmentLines.isNotEmpty() || matchingDrugsInMostRecentLineWithDate.isNotEmpty() -> {
                val drugList = Format.concatItemsWithAnd(matchingDrugsInUnknownTreatmentLines + matchingDrugsInMostRecentLineWithDate)
                EvaluationFactory.undetermined(labels.hasHadTreatmentWithDrugFromSetAsMostRecentUndetermined(drugList))
            }

            possibleTrialMatch(if (history.size == 1) history.first() else mostRecentTreatmentEntry) -> {
                EvaluationFactory.undetermined(
                    labels.hasHadTreatmentWithDrugFromSetAsMostRecentUndeterminedTrial(Format.concatItemsWithOr(drugsToMatch))
                )
            }

            history.flatMap { selectMatchingDrugsFromEntry(it, drugNamesToMatch) }.isNotEmpty() -> {
                EvaluationFactory.fail(
                    labels.hasHadTreatmentWithDrugFromSetAsMostRecentFailNotMostRecent(Format.concatItemsWithOr(drugsToMatch))
                )
            }

            else -> {
                EvaluationFactory.fail(
                    labels.hasHadTreatmentWithDrugFromSetAsMostRecentFailNotReceived(Format.concatItemsWithOr(drugsToMatch))
                )
            }
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