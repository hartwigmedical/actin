package com.hartwig.actin.algo.evaluation.medication

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.util.Format.concatLowercaseWithCommaAndAnd
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
                    "CYP$termToFind inhibiting medication use (${concatLowercaseWithCommaAndAnd(cypInhibitorsReceived)})"
                )
            }

            termToFind in MedicationConstants.UNDETERMINED_CYP_STRING -> {
                EvaluationFactory.undetermined(
                    "CYP$termToFind inhibiting medication use undetermined"
                )
            }

            cypInhibitorsPlanned.isNotEmpty() -> {
                EvaluationFactory.recoverableWarn(
                    "Planned CYP$termToFind inhibiting medication use (${concatLowercaseWithCommaAndAnd(cypInhibitorsPlanned)})"
                )
            }

            else -> {
                EvaluationFactory.recoverableFail(
                    "No CYP$termToFind inhibiting medication use "
                )
            }
        }
    }
}