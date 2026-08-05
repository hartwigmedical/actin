package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.EvaluationLabels
import com.hartwig.actin.algo.evaluation.treatment.TreatmentHistoryEntryFunctions.evaluateIfDrugHadPDResponse
import com.hartwig.actin.algo.evaluation.util.Format
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.treatment.Drug

class HasHadPDFollowingTreatmentWithAnyDrug(private val drugsToMatch: Set<Drug>, private val labels: EvaluationLabels.Treatment) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val treatmentEvaluation = evaluateIfDrugHadPDResponse(record.oncologicalHistory, drugsToMatch)

        return if (treatmentEvaluation.matchingDrugsWithPD.isNotEmpty()) {
            EvaluationFactory.pass(
                labels.hasHadPDFollowingTreatmentWithAnyDrugPass(Format.concatItemsWithAnd(treatmentEvaluation.matchingDrugsWithPD))
            )
        } else if (treatmentEvaluation.possibleTrialMatch) {
            EvaluationFactory.undetermined(labels.hasHadPDFollowingTreatmentWithAnyDrugUndeterminedTrial(Format.concatItemsWithOr(drugsToMatch)))
        } else if (treatmentEvaluation.matchesWithUnclearPD) {
            EvaluationFactory.undetermined(
                labels.hasHadPDFollowingTreatmentWithAnyDrugUndeterminedUnclearPd(Format.concatItemsWithAnd(treatmentEvaluation.matchingDrugs))
            )
        } else if (treatmentEvaluation.matchingDrugs.isNotEmpty()) {
            EvaluationFactory.fail(labels.hasHadPDFollowingTreatmentWithAnyDrugFailNoPd(Format.concatItemsWithAnd(treatmentEvaluation.matchingDrugs)))
        } else {
            EvaluationFactory.fail(labels.hasHadPDFollowingTreatmentWithAnyDrugFailNotReceived(Format.concatItemsWithOr(drugsToMatch)))
        }
    }
}
