package com.hartwig.actin.algo.evaluation.medication

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.EvaluationLabels
import com.hartwig.actin.algo.evaluation.util.Format.concatLowercaseWithCommaAndAnd
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.DrugInteraction

class CurrentlyGetsCypXInducingMedication(
    private val selector: MedicationSelector,
    private val termToFind: String,
    private val labels: EvaluationLabels.Medication
) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val medications = record.medications ?: return medicationNotProvided(labels)
        val cypInducersReceived =
            selector.activeWithInteraction(medications, termToFind, DrugInteraction.Type.INDUCER, DrugInteraction.Group.CYP)
                .map { it.name }.toSet()

        val cypInducersPlanned =
            selector.plannedWithInteraction(medications, termToFind, DrugInteraction.Type.INDUCER, DrugInteraction.Group.CYP)
                .map { it.name }.toSet()

        return when {
            cypInducersReceived.isNotEmpty() -> {
                EvaluationFactory.recoverablePass(
                    labels.currentlyGetsCypXInducingMedicationRecoverablePass(
                        termToFind, concatLowercaseWithCommaAndAnd(cypInducersReceived)
                    )
                )
            }

            termToFind in MedicationConstants.UNDETERMINED_CYP_STRING -> {
                EvaluationFactory.undetermined(labels.currentlyGetsCypXInducingMedicationUndetermined(termToFind))
            }

            cypInducersPlanned.isNotEmpty() -> {
                EvaluationFactory.warn(
                    labels.currentlyGetsCypXInducingMedicationWarn(termToFind, concatLowercaseWithCommaAndAnd(cypInducersPlanned))
                )
            }

            else -> {
                EvaluationFactory.recoverableFail(labels.currentlyGetsCypXInducingMedicationRecoverableFail(termToFind))
            }
        }
    }
}