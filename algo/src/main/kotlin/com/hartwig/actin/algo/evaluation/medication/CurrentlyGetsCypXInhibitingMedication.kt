package com.hartwig.actin.algo.evaluation.medication

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.EvaluationLabels
import com.hartwig.actin.algo.evaluation.util.Format.concatLowercaseWithCommaAndAnd
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.DrugInteraction

class CurrentlyGetsCypXInhibitingMedication(
    private val selector: MedicationSelector,
    private val termToFind: String,
    private val labels: EvaluationLabels.Medication
) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val medications = record.medications ?: return medicationNotProvided(labels)
        val cypInhibitorsReceived =
            selector.activeWithInteraction(medications, termToFind, DrugInteraction.Type.INHIBITOR, DrugInteraction.Group.CYP)
                .map { it.name }

        val cypInhibitorsPlanned =
            selector.plannedWithInteraction(medications, termToFind, DrugInteraction.Type.INHIBITOR, DrugInteraction.Group.CYP)
                .map { it.name }

        return when {
            cypInhibitorsReceived.isNotEmpty() -> {
                EvaluationFactory.recoverablePass(
                    labels.currentlyGetsCypXInhibitingMedicationRecoverablePass(
                        termToFind, concatLowercaseWithCommaAndAnd(cypInhibitorsReceived)
                    )
                )
            }

            termToFind in MedicationConstants.UNDETERMINED_CYP_STRING -> {
                EvaluationFactory.undetermined(labels.currentlyGetsCypXInhibitingMedicationUndetermined(termToFind))
            }

            cypInhibitorsPlanned.isNotEmpty() -> {
                EvaluationFactory.warn(
                    labels.currentlyGetsCypXInhibitingMedicationWarn(termToFind, concatLowercaseWithCommaAndAnd(cypInhibitorsPlanned))
                )
            }

            else -> {
                EvaluationFactory.recoverableFail(labels.currentlyGetsCypXInhibitingMedicationRecoverableFail(termToFind))
            }
        }
    }
}