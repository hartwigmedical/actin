package com.hartwig.actin.algo.evaluation.treatment


import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.clinical.datamodel.treatment.Drug
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.clinical.datamodel.treatment.DrugTreatment
import com.hartwig.actin.clinical.datamodel.treatment.history.TreatmentHistoryEntry

class HasHadTreatmentWithDrugAsMostRecent(private val drugToMatch: Set<Drug>) : EvaluationFunction {
    override fun evaluate(record: PatientRecord): Evaluation {
        val mostRecentTreatmentEntry: TreatmentHistoryEntry = record.oncologicalHistory.maxWithOrNull(TreatmentHistoryEntryStartDateComparator())
            ?: return EvaluationFactory.fail("No prior treatments in history")
        val historyContainsTreatmentsWithUnknownStartDate = record.oncologicalHistory.any { it.startYear == null }

        val matchingDrugsInMostRecentLine = mostRecentTreatmentEntry.allTreatments().flatMap { (it as? DrugTreatment)?.drugs ?: emptyList() }
            .filter { it.name.lowercase() in drugToMatch.map { drug -> drug.name.lowercase() } }

        val matchingDrugsInAllTreatmentLines = record.oncologicalHistory
            .flatMap { it.allTreatments() }
            .flatMap { (it as? DrugTreatment)?.drugs ?: emptyList() }
            .filter { it.name.lowercase() in drugToMatch.map { drug -> drug.name.lowercase() } }

        val possibleTrialMatch = mostRecentTreatmentEntry.isTrial && mostRecentTreatmentEntry.allTreatments()
            .any { (it as? DrugTreatment)?.drugs.isNullOrEmpty() }
        val matchingDrugDisplay = matchingDrugsInMostRecentLine.joinToString(" and ") { it.display() }
        val drugsToMatchDisplay = "received ${drugToMatch.joinToString(" or ") { it.display() } } as most recent treatment line"

        return when {
            matchingDrugsInMostRecentLine.isNotEmpty() -> {
                EvaluationFactory.pass(
                    "Patient has received $matchingDrugDisplay as most recent treatment",
                    "Has received $matchingDrugDisplay as most recent treatment"
                )
            }

            historyContainsTreatmentsWithUnknownStartDate && matchingDrugsInAllTreatmentLines.isNotEmpty() -> {
                val display = "Has received ${matchingDrugsInAllTreatmentLines.joinToString(" and ") { it.display() }} " +
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

            else -> {
                EvaluationFactory.fail(
                    "Patient has not $drugsToMatchDisplay",
                    "Has not $drugsToMatchDisplay"
                )
            }
        }
    }
}