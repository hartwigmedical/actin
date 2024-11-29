package com.hartwig.actin.algo.evaluation.medication

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.util.Format
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.DrugInteraction

class CurrentlyGetsCypXInhibitingMedication(private val selector: MedicationSelector, private val termToFind: String) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val medications = record.medications ?: return MEDICATION_NOT_PROVIDED
        val cypInhibitorsReceived =
            selector.activeWithInteraction(medications, termToFind, DrugInteraction.Type.INHIBITOR, DrugInteraction.Group.CYP)
                .map { it.name }

        val cypInhibitorsPlanned =
            selector.plannedWithInteraction(medications, termToFind, DrugInteraction.Type.INHIBITOR, DrugInteraction.Group.CYP)
                .map { it.name }

        return when {
            cypInhibitorsReceived.isNotEmpty() -> {
                EvaluationFactory.recoverablePass(
                    "Patient currently gets CYP$termToFind inhibiting medication: ${Format.concatLowercaseWithAnd(cypInhibitorsReceived)}",
                    "CYP$termToFind inhibiting medication use: ${Format.concatLowercaseWithAnd(cypInhibitorsReceived)}"
                )
            }

            termToFind in MedicationRuleMapper.UNDETERMINED_CYP_STRING -> {
                EvaluationFactory.undetermined(
                    "Undetermined if patient currently gets CYP$termToFind inhibiting medication",
                    "Undetermined CYP$termToFind inhibiting medication use"
                )
            }

            cypInhibitorsPlanned.isNotEmpty() -> {
                EvaluationFactory.recoverableWarn(
                    "Patient plans to get CYP$termToFind inhibiting medication: ${Format.concatLowercaseWithAnd(cypInhibitorsPlanned)}",
                    "Planned CYP$termToFind inhibiting medication use: ${Format.concatLowercaseWithAnd(cypInhibitorsPlanned)}"
                )
            }

            else -> {
                EvaluationFactory.recoverableFail(
                    "Patient currently does not get CYP$termToFind inhibiting medication ",
                    "No CYP$termToFind inhibiting medication use "
                )
            }
        }
    }
}