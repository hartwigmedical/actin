package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.clinical.interpretation.ProgressiveDiseaseFunctions
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.clinical.treatment.Drug
import com.hartwig.actin.datamodel.clinical.treatment.DrugTreatment
import com.hartwig.actin.datamodel.clinical.treatment.history.StopReason

object TreatmentHistoryEvaluationFunctions {

    fun evaluateTreatmentHistory(record: PatientRecord, drugsToMatch: Set<Drug>): TreatmentHistoryEvaluation {
        val treatmentHistory = record.oncologicalHistory

        val allowTrialMatches = drugsToMatch.map(Drug::category).all(TrialFunctions::categoryAllowsTrialMatches)

        return treatmentHistory.map { entry ->
            val categoriesToMatch = drugsToMatch.map(Drug::category).toSet()
            val isPD = ProgressiveDiseaseFunctions.treatmentResultedInPD(entry)
            val matchingDrugs = entry.allTreatments().flatMap {
                (it as? DrugTreatment)?.drugs?.intersect(drugsToMatch) ?: emptyList()
            }.toSet()
            val possibleTrialMatch =
                entry.isTrial && (entry.categories().isEmpty() || entry.categories().intersect(categoriesToMatch).isNotEmpty())
                        && allowTrialMatches
            val matchesWithToxicity = entry.treatmentHistoryDetails?.stopReason == StopReason.TOXICITY
            if (matchingDrugs.isNotEmpty()) {
                TreatmentHistoryEvaluation(
                    matchingDrugsWithPD = if (isPD == true) matchingDrugs else emptySet(),
                    matchingDrugs = matchingDrugs,
                    matchesWithUnclearPD = isPD == null,
                    possibleTrialMatch = possibleTrialMatch,
                    matchesWithToxicity = matchesWithToxicity
                )
            } else {
                TreatmentHistoryEvaluation(possibleTrialMatch = possibleTrialMatch)
            }
        }.fold(TreatmentHistoryEvaluation()) { acc, result ->
            TreatmentHistoryEvaluation(
                matchingDrugsWithPD = acc.matchingDrugsWithPD + result.matchingDrugsWithPD,
                matchingDrugs = acc.matchingDrugs + result.matchingDrugs,
                matchesWithUnclearPD = acc.matchesWithUnclearPD || result.matchesWithUnclearPD,
                possibleTrialMatch = acc.possibleTrialMatch || result.possibleTrialMatch,
                matchesWithToxicity = acc.matchesWithToxicity || result.matchesWithToxicity
            )
        }
    }

    data class TreatmentHistoryEvaluation(
        val matchingDrugsWithPD: Set<Drug> = emptySet(),
        val matchingDrugs: Set<Drug> = emptySet(),
        val matchesWithUnclearPD: Boolean = false,
        val possibleTrialMatch: Boolean = false,
        val matchesWithToxicity: Boolean = false
    )
}