package com.hartwig.actin.algo.evaluation.medication

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.EvaluationLabels
import com.hartwig.actin.algo.evaluation.util.Format.concatLowercaseWithCommaAndAnd
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.DrugInteraction

class CurrentlyGetsCypXSubstrateMedication(
    private val selector: MedicationSelector,
    private val termToFind: String,
    private val labels: EvaluationLabels.Medication
) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val medications = record.medications ?: return medicationNotProvided(labels)
        val cypSubstratesReceived =
            selector.activeWithInteraction(medications, termToFind, DrugInteraction.Type.SUBSTRATE, DrugInteraction.Group.CYP)
                .map { it.name }

        val cypSubstratesPlanned =
            selector.plannedWithInteraction(medications, termToFind, DrugInteraction.Type.SUBSTRATE, DrugInteraction.Group.CYP)
                .map { it.name }

        return when {
            cypSubstratesReceived.isNotEmpty() -> {
                EvaluationFactory.recoverablePass(
                    labels.currentlyGetsCypXSubstrateMedicationRecoverablePass(
                        termToFind, concatLowercaseWithCommaAndAnd(cypSubstratesReceived)
                    )
                )
            }

            termToFind in MedicationConstants.UNDETERMINED_CYP_STRING -> {
                EvaluationFactory.undetermined(labels.currentlyGetsCypXSubstrateMedicationUndetermined(termToFind))
            }

            cypSubstratesPlanned.isNotEmpty() -> {
                EvaluationFactory.warn(
                    labels.currentlyGetsCypXSubstrateMedicationWarn(termToFind, concatLowercaseWithCommaAndAnd(cypSubstratesPlanned))
                )
            }

            else -> {
                EvaluationFactory.recoverableFail(labels.currentlyGetsCypXSubstrateMedicationRecoverableFail(termToFind))
            }
        }
    }
}