package com.hartwig.actin.algo.evaluation.treatment


import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.clinical.datamodel.treatment.Drug
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.util.Format
import com.hartwig.actin.clinical.datamodel.treatment.DrugTreatment
import com.hartwig.actin.clinical.datamodel.treatment.history.TreatmentHistoryEntry

class HasHadTreatmentWithDrugFromSetAsMostRecent(private val drugsToMatch: Set<Drug>) : EvaluationFunction {
    override fun evaluate(record: PatientRecord): Evaluation {

        if (record.oncologicalHistory.isEmpty()) return EvaluationFactory.fail("No prior treatments in history")
        val (historyWithoutDates, historyWithDates) = record.oncologicalHistory.partition { it.startYear == null }
        val mostRecentTreatmentEntry = historyWithDates.maxWithOrNull(TreatmentHistoryEntryStartDateComparator())

        val drugNamesToMatch = drugsToMatch.map { drug -> drug.name.lowercase() }
        val matchingDrugsInMostRecentLine = selectMatchingDrugsFromEntry(mostRecentTreatmentEntry, drugNamesToMatch)
        val matchingDrugsInHistoryWithoutDates = historyWithoutDates.flatMap { selectMatchingDrugsFromEntry(it, drugNamesToMatch) }
        val matchingDrugsInAllTreatmentLines = record.oncologicalHistory.flatMap { selectMatchingDrugsFromEntry(it, drugNamesToMatch) }

        val mostRecentTreatments = mostRecentTreatmentEntry?.allTreatments() ?: record.oncologicalHistory.first().allTreatments()
        val possibleTrialMatch = mostRecentTreatmentEntry?.isTrial == true && (mostRecentTreatments.isEmpty() ||
                mostRecentTreatments.flatMap { (it as? DrugTreatment)?.drugs ?: emptySet()}
                    .any { drugInHistory -> drugsToMatch.any { drugsToMatch -> drugsToMatch.category == drugInHistory.category} }
                )
        val drugsToMatchDisplay = "received ${Format.concatItemsWithOr(drugsToMatch)}"

        return when {
            (matchingDrugsInMostRecentLine.isNotEmpty()
                    || (matchingDrugsInHistoryWithoutDates.isNotEmpty() && record.oncologicalHistory.size == 1)) -> {
                val matchingDrugDisplay = Format.concatItemsWithAnd(matchingDrugsInMostRecentLine)
                EvaluationFactory.pass(
                    "Patient has received $matchingDrugDisplay as most recent treatment",
                    "Has received $matchingDrugDisplay as most recent treatment"
                )
            }

            matchingDrugsInHistoryWithoutDates.isNotEmpty() -> {
                val display = "Has received ${Format.concatItemsWithAnd(matchingDrugsInAllTreatmentLines)} " +
                        "but undetermined if most recent"
                EvaluationFactory.undetermined(
                    "$display (dates missing in treatment list)",
                    display
                )
            }

            possibleTrialMatch -> {
                EvaluationFactory.undetermined(
                    "Undetermined if patient has $drugsToMatchDisplay - exact drugs in recent trial unknown",
                    "Undetermined if $drugsToMatchDisplay - exact drugs in recent trial unknown"
                )
            }

            matchingDrugsInAllTreatmentLines.isNotEmpty() -> {
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

    private fun selectMatchingDrugsFromEntry(
        treatmentHistoryEntry: TreatmentHistoryEntry?, drugNamesToMatch: List<String>
    ): List<Drug> {
        return treatmentHistoryEntry?.allTreatments()
            ?.flatMap { (it as? DrugTreatment)?.drugs ?: emptyList() }
            ?.filter { it.name.lowercase() in drugNamesToMatch }
            ?: emptyList()
    }
}