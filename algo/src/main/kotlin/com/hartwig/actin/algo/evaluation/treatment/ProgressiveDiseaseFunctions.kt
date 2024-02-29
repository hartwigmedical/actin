package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.evaluation.util.DateComparison
import com.hartwig.actin.clinical.datamodel.treatment.Drug
import com.hartwig.actin.clinical.datamodel.treatment.DrugTreatment
import com.hartwig.actin.clinical.datamodel.treatment.history.StopReason
import com.hartwig.actin.clinical.datamodel.treatment.history.TreatmentHistoryEntry
import com.hartwig.actin.clinical.datamodel.treatment.history.TreatmentResponse

private const val MIN_WEEKS_TO_ASSUME_STOP_DUE_TO_PD = 26 // half year

object ProgressiveDiseaseFunctions {

    fun treatmentResultedInPD(treatment: TreatmentHistoryEntry): Boolean? {
        val bestResponse = treatment.treatmentHistoryDetails?.bestResponse
        val stopReason = treatment.treatmentHistoryDetails?.stopReason
        val treatmentDuration = DateComparison.minWeeksBetweenDates(
            treatment.startYear,
            treatment.startMonth,
            treatment.treatmentHistoryDetails?.stopYear,
            treatment.treatmentHistoryDetails?.stopMonth
        )

        return when {
            bestResponse == TreatmentResponse.PROGRESSIVE_DISEASE || stopReason == StopReason.PROGRESSIVE_DISEASE -> true

            stopReason == null && treatmentDuration != null && treatmentDuration > MIN_WEEKS_TO_ASSUME_STOP_DUE_TO_PD -> true

            stopReason != null -> false

            else -> null
        }
    }

    fun evaluateTreatmentHistory(record: PatientRecord, drugsToMatch: Set<Drug>): TreatmentHistoryEvaluation {
        val treatmentHistory = record.clinical.oncologicalHistory

        val allowTrialMatches = drugsToMatch.map(Drug::category).all(TrialFunctions::categoryAllowsTrialMatches)

        return treatmentHistory.map { entry ->
            val categoriesToMatch = drugsToMatch.map(Drug::category).toSet()
            val isPD = treatmentResultedInPD(entry)
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