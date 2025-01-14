package com.hartwig.actin.algo.evaluation.medication

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.util.Format.concatLowercaseWithCommaAndAnd
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.DrugInteraction

class CurrentlyGetsCypXSubstrateMedication(private val selector: MedicationSelector, private val termToFind: String) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val medications = record.medications ?: return MEDICATION_NOT_PROVIDED
        val cypSubstratesReceived =
            selector.activeWithInteraction(medications, termToFind, DrugInteraction.Type.SUBSTRATE, DrugInteraction.Group.CYP)
                .map { it.name }

        val cypSubstratesPlanned =
            selector.plannedWithInteraction(medications, termToFind, DrugInteraction.Type.SUBSTRATE, DrugInteraction.Group.CYP)
                .map { it.name }

        return when {
            cypSubstratesReceived.isNotEmpty() -> {
                EvaluationFactory.recoverablePass(
                    "CYP$termToFind substrate medication use (${concatLowercaseWithCommaAndAnd(cypSubstratesReceived)})"
                )
            }

            termToFind in MedicationConstants.UNDETERMINED_CYP_STRING -> {
                EvaluationFactory.undetermined("CYP$termToFind substrate medication use undetermined")
            }

            cypSubstratesPlanned.isNotEmpty() -> {
                EvaluationFactory.recoverableWarn(
                    "Planned CYP$termToFind substrate medication use (${concatLowercaseWithCommaAndAnd(cypSubstratesPlanned)})"
                )
            }

            else -> {
                EvaluationFactory.recoverableFail(
                    "No CYP$termToFind substrate medication use"
                )
            }
        }
    }
}