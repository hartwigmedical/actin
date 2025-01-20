package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.treatment.ProgressiveDiseaseFunctions.evaluateTreatmentHistory
import com.hartwig.actin.algo.evaluation.util.Format
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.treatment.Drug

class HasAcquiredResistanceToAnyDrug(private val drugsToMatch: Set<Drug>) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {

        val treatmentEvaluation = evaluateTreatmentHistory(record, drugsToMatch)
        val toxicityMessage =
            if (treatmentEvaluation.matchesWithToxicity) "(stop reason toxicity) " else ""

        return when {
            treatmentEvaluation.matchingDrugsWithPD.isNotEmpty() -> {
                EvaluationFactory.pass("Has potential acquired resistance to ${Format.concatItemsWithAnd(treatmentEvaluation.matchingDrugsWithPD)}")
            }

            (treatmentEvaluation.possibleTrialMatch) -> {
                EvaluationFactory.undetermined(
                    "Undetermined resistance to ${Format.concatItemsWithOr(drugsToMatch)} since unknown if drug was included in previous trial"
                )
            }

            (treatmentEvaluation.matchesWithUnclearPD || treatmentEvaluation.matchesWithToxicity) -> {
                EvaluationFactory.undetermined("Undetermined acquired resistance to ${Format.concatItemsWithOr(drugsToMatch)} $toxicityMessage")
            }

            (treatmentEvaluation.matchingDrugs.isNotEmpty()) -> {
                EvaluationFactory.fail(
                    "Has received drugs ${Format.concatItemsWithAnd(treatmentEvaluation.matchingDrugs)} but " +
                            "no progressive disease"
                )
            }

            else -> {
                EvaluationFactory.fail("No acquired resistance to ${Format.concatItemsWithOr(drugsToMatch)} since drug not in treatment history")
            }
        }
    }
}