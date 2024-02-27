package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.treatment.ProgressiveDiseaseFunctions
import com.hartwig.actin.algo.evaluation.treatment.TrialFunctions
import com.hartwig.actin.algo.evaluation.util.Format
import com.hartwig.actin.clinical.datamodel.treatment.Treatment
import com.hartwig.actin.clinical.datamodel.treatment.history.StopReason

class HasAcquiredResistanceToSomeTreatment(private val treatmentToMatch: Treatment) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {

        val treatmentEvaluation = evaluateTreatmentHistory(record)
        val toxicityMessage =
            if (treatmentEvaluation.matchesWithToxicity) "(stop reason toxicity) " else ""

        return when {
            treatmentEvaluation.matchingTreatmentWithPD.isNotEmpty() -> {
                EvaluationFactory.pass(
                    "Patient has a tumor with potential acquired resistance to ${treatmentToMatch.name}",
                    "Has potential acquired resistance to ${treatmentToMatch.name}"
                )
            }
            (treatmentEvaluation.possibleTrialMatch) -> {
                EvaluationFactory.undetermined(
                    "Undetermined if patient has acquired resistance to ${treatmentToMatch.name} since unknown if treatment " +
                            "received in trial included ${treatmentToMatch.name}",
                    "Undetermined resistance to ${treatmentToMatch.name} since unknown if treatment in trial included ${treatmentToMatch.name}"
                )
            }

            (treatmentEvaluation.matchesWithUnclearPD || treatmentEvaluation.matchesWithToxicity) -> {
                EvaluationFactory.undetermined(
                    "Undetermined acquired resistance to ${treatmentToMatch.name} $toxicityMessage- assuming none",
                    "Undetermined resistance to ${treatmentToMatch.name} $toxicityMessage- assuming none"
                )
            }

            (treatmentEvaluation.matchingTreatment.isNotEmpty()) -> {
                EvaluationFactory.fail("Has received drugs ${Format.concatItemsWithAnd(treatmentEvaluation.matchingTreatment)} but no progressive disease")
            }

            else -> {
                EvaluationFactory.fail(
                    "Patient was not treated with ${treatmentToMatch.name} hence does not have acquired resistance to this drug",
                    "No acquired resistance to ${treatmentToMatch.name} since not in treatment history"
                )
            }
        }
    }

    private fun evaluateTreatmentHistory(record: PatientRecord): TreatmentHistoryEvaluation {
        val treatmentHistory = record.clinical.oncologicalHistory

        val trialCategoriesToMatch = treatmentToMatch.categories().filter(TrialFunctions::categoryAllowsTrialMatches).toSet()

        return treatmentHistory.map { entry ->
            val isPD = ProgressiveDiseaseFunctions.treatmentResultedInPD(entry)
            val matchingDrugs = entry.allTreatments().filter {
                treatment -> treatment == treatmentToMatch }
            .toSet()
            val possibleTrialMatch =
                entry.isTrial && (entry.categories().isEmpty() || entry.categories().intersect(trialCategoriesToMatch).isNotEmpty())
            val matchesWithToxicity = entry.treatmentHistoryDetails?.stopReason == StopReason.TOXICITY
            if (matchingDrugs.isNotEmpty()) {
                TreatmentHistoryEvaluation(
                    matchingTreatmentWithPD = if (isPD == true) matchingDrugs else emptySet(),
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
                matchingTreatmentWithPD = acc.matchingTreatmentWithPD + result.matchingTreatmentWithPD,
                matchingTreatment = acc.matchingTreatment + result.matchingTreatment,
                matchesWithUnclearPD = acc.matchesWithUnclearPD || result.matchesWithUnclearPD,
                possibleTrialMatch = acc.possibleTrialMatch || result.possibleTrialMatch,
                matchesWithToxicity = acc.matchesWithToxicity || result.matchesWithToxicity
            )
        }
    }

    private data class TreatmentHistoryEvaluation(
        val matchingTreatmentWithPD: Set<Treatment> = emptySet(),
        val matchingTreatment: Set<Treatment> = emptySet(),
        val matchesWithUnclearPD: Boolean = false,
        val possibleTrialMatch: Boolean = false,
        val matchesWithToxicity: Boolean = false
    )

}