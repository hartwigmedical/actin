package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.treatment.ProgressiveDiseaseFunctions.evaluateTreatmentHistory
import com.hartwig.actin.algo.evaluation.util.Format
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.treatment.Drug

//TODO (CB): only 1 message
class HasHadPDFollowingTreatmentWithAnyDrug(private val drugsToMatch: Set<Drug>) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val treatmentEvaluation = evaluateTreatmentHistory(record, drugsToMatch)

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
}