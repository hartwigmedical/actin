package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.EvaluationLabels
import com.hartwig.actin.algo.evaluation.treatment.TreatmentHistoryEntryFunctions.evaluateIfDrugHadPDResponse
import com.hartwig.actin.algo.evaluation.util.Format
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.treatment.Drug

class HasAcquiredResistanceToAnyDrug(private val drugsToMatch: Set<Drug>, private val labels: EvaluationLabels.Treatment) :
    EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {

        val treatmentEvaluation = evaluateIfDrugHadPDResponse(record.oncologicalHistory, drugsToMatch)
        val toxicitySuffix = if (treatmentEvaluation.matchesWithToxicity) labels.hasAcquiredResistanceToAnyDrugToxicitySuffix() else ""

        return when {
            treatmentEvaluation.matchingDrugsWithPD.isNotEmpty() -> {
                EvaluationFactory.pass(
                    labels.hasAcquiredResistanceToAnyDrugPass(Format.concatItemsWithAnd(treatmentEvaluation.matchingDrugsWithPD))
                )
            }

            treatmentEvaluation.possibleTrialMatch -> {
                EvaluationFactory.undetermined(
                    labels.hasAcquiredResistanceToAnyDrugUndeterminedTrial(Format.concatItemsWithOr(drugsToMatch))
                )
            }

            treatmentEvaluation.matchesWithUnclearPD || treatmentEvaluation.matchesWithToxicity -> {
                EvaluationFactory.undetermined(
                    labels.hasAcquiredResistanceToAnyDrugUndetermined(Format.concatItemsWithOr(drugsToMatch), toxicitySuffix)
                )
            }

            treatmentEvaluation.matchingDrugs.isNotEmpty() -> {
                EvaluationFactory.fail(
                    labels.hasAcquiredResistanceToAnyDrugFailReceivedNoPD(Format.concatItemsWithAnd(treatmentEvaluation.matchingDrugs))
                )
            }

            else -> {
                EvaluationFactory.fail(labels.hasAcquiredResistanceToAnyDrugFail(Format.concatItemsWithOr(drugsToMatch)))
            }
        }
    }
}