package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.treatment.ProgressiveDiseaseFunctions.evaluateTreatmentHistory
import com.hartwig.actin.algo.evaluation.util.Format
import com.hartwig.actin.clinical.datamodel.treatment.Drug

class HasAcquiredResistanceToSomeDrug(private val drugsToMatch: Set<Drug>) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {

        val treatmentEvaluation = evaluateTreatmentHistory(record, drugsToMatch)
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

            (treatmentEvaluation.matchingDrugs.isNotEmpty()) -> {
                EvaluationFactory.fail("Has received drugs ${Format.concatItemsWithAnd(treatmentEvaluation.matchingDrugs)} but " +
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
}