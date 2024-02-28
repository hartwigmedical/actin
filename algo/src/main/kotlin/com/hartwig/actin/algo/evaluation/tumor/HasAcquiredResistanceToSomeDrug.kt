package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.treatment.ProgressiveDiseaseFunctions
import com.hartwig.actin.algo.evaluation.treatment.TrialFunctions
import com.hartwig.actin.algo.evaluation.util.Format
import com.hartwig.actin.clinical.datamodel.treatment.Drug
import com.hartwig.actin.clinical.datamodel.treatment.DrugTreatment
import com.hartwig.actin.clinical.datamodel.treatment.history.StopReason

class HasAcquiredResistanceToSomeDrug(private val drugsToMatch: Set<Drug>) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {

        val treatmentEvaluation = evaluateTreatmentHistory(record)
        val toxicityMessage =
            if (treatmentEvaluation.matchesWithToxicity) "(stop reason toxicity) " else ""

        return when {
            treatmentEvaluation.matchingDrugsWithPD.isNotEmpty() -> {
                EvaluationFactory.pass(
                    "Patient has a tumor with potential acquired resistance to drugs " +
                            Format.concatItemsWithAnd(treatmentEvaluation.matchingDrugsWithPD),
                    "Has potential acquired resistance to ${Format.concatItemsWithAnd(treatmentEvaluation.matchingDrugsWithPD)}"
                )
            }
            (treatmentEvaluation.possibleTrialMatch) -> {
                EvaluationFactory.undetermined(
                    "Undetermined if patient has acquired resistance to ${Format.concatItemsWithOr(drugsToMatch)} since unknown if " +
                            "treatment received in trial included ${Format.concatItemsWithOr(drugsToMatch)}",
                    "Undetermined resistance to ${Format.concatItemsWithOr(drugsToMatch)} since unknown if treatment in trial included " +
                            Format.concatItemsWithOr(drugsToMatch)
                )
            }

            (treatmentEvaluation.matchesWithUnclearPD || treatmentEvaluation.matchesWithToxicity) -> {
                EvaluationFactory.undetermined(
                    "Undetermined acquired resistance to ${Format.concatItemsWithOr(drugsToMatch)} $toxicityMessage- assuming none",
                    "Undetermined resistance to ${Format.concatItemsWithOr(drugsToMatch)} $toxicityMessage- assuming none"
                )
            }

            (treatmentEvaluation.matchingTreatment.isNotEmpty()) -> {
                EvaluationFactory.fail("Has received drugs ${Format.concatItemsWithAnd(treatmentEvaluation.matchingTreatment)} but " +
                        "no progressive disease")
            }

            else -> {
                EvaluationFactory.fail(
                    "Patient was not treated with ${Format.concatItemsWithOr(drugsToMatch)} hence does not have acquired resistance to " +
                            Format.concatItemsWithOr(drugsToMatch),
                    "No acquired resistance to ${Format.concatItemsWithOr(drugsToMatch)} since not in treatment history"
                )
            }
        }
    }

    private fun evaluateTreatmentHistory(record: PatientRecord): TreatmentHistoryEvaluation {
        val treatmentHistory = record.clinical.oncologicalHistory

        val trialCategoriesToMatch = drugsToMatch.map(Drug::category).filter(TrialFunctions::categoryAllowsTrialMatches).toSet()

        return treatmentHistory.map { entry ->
            val isPD = ProgressiveDiseaseFunctions.treatmentResultedInPD(entry)
            val matchingDrugs = entry.allTreatments().flatMap {
                (it as? DrugTreatment)?.drugs?.intersect(drugsToMatch) ?: emptyList()
            }.toSet()
            val possibleTrialMatch =
                entry.isTrial && (entry.categories().isEmpty() || entry.categories().intersect(trialCategoriesToMatch).isNotEmpty())
            val matchesWithToxicity = entry.treatmentHistoryDetails?.stopReason == StopReason.TOXICITY
            if (matchingDrugs.isNotEmpty()) {
                TreatmentHistoryEvaluation(
                    matchingDrugsWithPD = if (isPD == true) matchingDrugs else emptySet(),
                    matchingTreatment = matchingDrugs,
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
                matchingTreatment = acc.matchingTreatment + result.matchingTreatment,
                matchesWithUnclearPD = acc.matchesWithUnclearPD || result.matchesWithUnclearPD,
                possibleTrialMatch = acc.possibleTrialMatch || result.possibleTrialMatch,
                matchesWithToxicity = acc.matchesWithToxicity || result.matchesWithToxicity
            )
        }
    }

    private data class TreatmentHistoryEvaluation(
        val matchingDrugsWithPD: Set<Drug> = emptySet(),
        val matchingTreatment: Set<Drug> = emptySet(),
        val matchesWithUnclearPD: Boolean = false,
        val possibleTrialMatch: Boolean = false,
        val matchesWithToxicity: Boolean = false
    )

}