package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.treatment.TreatmentHistoryEntryFunctions.evaluateIfDrugHadPDResponse
import com.hartwig.actin.algo.evaluation.util.Format
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.treatment.Drug

class HasHadPDFollowingTreatmentWithAnyDrug(private val drugsToMatch: Set<Drug>) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val treatmentEvaluation = evaluateIfDrugHadPDResponse(record.oncologicalHistory, drugsToMatch)

        return if (treatmentEvaluation.matchingDrugsWithPD.isNotEmpty()) {
            EvaluationFactory.pass(
                "PD after receiving drugs ${Format.concatItemsWithAnd(treatmentEvaluation.matchingDrugsWithPD)} based on provided treatments"
            )
        } else if (treatmentEvaluation.possibleTrialMatch) {
            EvaluationFactory.undetermined(
                "Undetermined if treatment from previous trial included ${
                    Format.concatItemsWithOr(
                        drugsToMatch
                    )
                }"
            )
        } else if (treatmentEvaluation.matchesWithUnclearPD) {
            EvaluationFactory.undetermined(
                "Drugs ${Format.concatItemsWithAnd(treatmentEvaluation.matchingDrugs)} in provided treatments but undetermined if PD"
            )
        } else if (treatmentEvaluation.matchingDrugs.isNotEmpty()) {
            EvaluationFactory.fail("Drugs ${Format.concatItemsWithAnd(treatmentEvaluation.matchingDrugs)} in provided treatments but no PD")
        } else {
            EvaluationFactory.fail("No treatments that include ${Format.concatItemsWithOr(drugsToMatch)} in provided treatments")
        }
    }
}