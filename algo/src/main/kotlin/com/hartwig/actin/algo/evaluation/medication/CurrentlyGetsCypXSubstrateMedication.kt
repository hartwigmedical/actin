package com.hartwig.actin.algo.evaluation.medication

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.util.Format
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
                    "Patient currently gets CYP$termToFind substrate medication: ${
                        Format.concatLowercaseWithAnd(
                            cypSubstratesReceived
                        )
                    }",
                    "CYP$termToFind substrate medication use: ${Format.concatLowercaseWithAnd(cypSubstratesReceived)}"
                )
            }

            termToFind in MedicationRuleMapper.UNDETERMINED_CYP.toString().substring(3) -> {
                EvaluationFactory.undetermined(
                    "Undetermined if patient currently gets CYP$termToFind substrate medication",
                    "Undetermined CYP$termToFind substrate medication use"
                )
            }

            cypSubstratesPlanned.isNotEmpty() -> {
                EvaluationFactory.recoverableWarn(
                    "Patient plans to get CYP$termToFind substrate medication: ${Format.concatLowercaseWithAnd(cypSubstratesPlanned)}",
                    "Planned CYP$termToFind substrate medication use: ${Format.concatLowercaseWithAnd(cypSubstratesPlanned)}"
                )
            }

            else -> {
                EvaluationFactory.recoverableFail(
                    "Patient currently does not get CYP$termToFind substrate medication ",
                    "No CYP$termToFind substrate medication use "
                )
            }
        }
    }
}