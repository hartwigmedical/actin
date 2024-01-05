package com.hartwig.actin.algo.evaluation.medication

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.util.Format
import com.hartwig.actin.clinical.datamodel.CypInteraction

class CurrentlyGetsCypXSubstrateMedication(
    private val selector: MedicationSelector,
    private val termToFind: String
) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val cypSubstratesReceived = selector.activeWithCypInteraction(
            record.clinical.medications, termToFind, CypInteraction.Type.SUBSTRATE
        ).map { it.name }

        return when {
            cypSubstratesReceived.isNotEmpty() -> {
                EvaluationFactory.recoverablePass(
                    "Patient currently gets CYP$termToFind substrate medication: ${Format.concatLowercaseWithAnd(cypSubstratesReceived)}",
                    "CYP$termToFind substrate medication use: ${Format.concatLowercaseWithAnd(cypSubstratesReceived)}"
                )
            }

            termToFind in MedicationRuleMapper.UNDETERMINED_CYP -> {
                EvaluationFactory.undetermined(
                    "Undetermined if patient currently gets CYP$termToFind substrate medication",
                    "Undetermined CYP$termToFind substrate medication use"
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