package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.treatment.ProgressiveDiseaseFunctions.treatmentResultedInPD
import com.hartwig.actin.algo.evaluation.util.Format
import com.hartwig.actin.clinical.datamodel.treatment.Drug
import com.hartwig.actin.clinical.datamodel.treatment.DrugTreatment

class HasHadPDFollowingTreatmentWithAnyDrug(private val drugsToMatch: Set<Drug>) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val treatmentEvaluation = evaluateTreatmentHistory(record)

        return if (treatmentEvaluation.matchingDrugsWithPD.isNotEmpty()) {
            EvaluationFactory.pass(
                "Has had PD after receiving drugs ${Format.concatItemsWithAnd(treatmentEvaluation.matchingDrugsWithPD)}"
            )
        } else if (treatmentEvaluation.possibleTrialMatch) {
            EvaluationFactory.undetermined("Undetermined if treatment received in trial included ${Format.concatItemsWithOr(drugsToMatch)}")
        } else if (treatmentEvaluation.matchesWithUnclearPD) {
            EvaluationFactory.undetermined(
                "Has received drugs ${Format.concatItemsWithAnd(treatmentEvaluation.matchingDrugs)} but undetermined if PD"
            )
        } else if (treatmentEvaluation.matchingDrugs.isNotEmpty()) {
            EvaluationFactory.fail("Has received drugs ${Format.concatItemsWithAnd(treatmentEvaluation.matchingDrugs)} but no PD")
        } else {
            EvaluationFactory.fail("Has not received treatments that include ${Format.concatItemsWithOr(drugsToMatch)}")
        }
    }

    private fun evaluateTreatmentHistory(record: PatientRecord): TreatmentHistoryEvaluation {
        val treatmentHistory = record.clinical.oncologicalHistory

        val trialCategoriesToMatch = drugsToMatch.map(Drug::category).filter(TrialFunctions::categoryAllowsTrialMatches).toSet()
        
        return treatmentHistory.map { entry ->
            val isPD = treatmentResultedInPD(entry)
            val matchingDrugs = entry.allTreatments().flatMap {
                (it as? DrugTreatment)?.drugs?.intersect(drugsToMatch) ?: emptyList()
            }.toSet()
            val possibleTrialMatch =
                entry.isTrial && (entry.categories().isEmpty() || entry.categories().intersect(trialCategoriesToMatch).isNotEmpty())
            if (matchingDrugs.isNotEmpty()) {
                TreatmentHistoryEvaluation(
                    matchingDrugsWithPD = if (isPD == true) matchingDrugs else emptySet(),
                    matchingDrugs = matchingDrugs,
                    matchesWithUnclearPD = isPD == null,
                    possibleTrialMatch = possibleTrialMatch
                )
            } else {
                TreatmentHistoryEvaluation(possibleTrialMatch = possibleTrialMatch)
            }
        }.fold(TreatmentHistoryEvaluation()) { acc, result ->
            TreatmentHistoryEvaluation(
                matchingDrugsWithPD = acc.matchingDrugsWithPD + result.matchingDrugsWithPD,
                matchingDrugs = acc.matchingDrugs + result.matchingDrugs,
                matchesWithUnclearPD = acc.matchesWithUnclearPD || result.matchesWithUnclearPD,
                possibleTrialMatch = acc.possibleTrialMatch || result.possibleTrialMatch
            )
        }
    }

    private data class TreatmentHistoryEvaluation(
        val matchingDrugsWithPD: Set<Drug> = emptySet(),
        val matchingDrugs: Set<Drug> = emptySet(),
        val matchesWithUnclearPD: Boolean = false,
        val possibleTrialMatch: Boolean = false
    )
}