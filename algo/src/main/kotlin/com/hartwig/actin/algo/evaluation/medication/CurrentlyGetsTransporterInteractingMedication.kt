package com.hartwig.actin.algo.evaluation.medication

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.EvaluationLabels
import com.hartwig.actin.algo.evaluation.util.Format.concatLowercaseWithCommaAndAnd
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.DrugInteraction

class CurrentlyGetsTransporterInteractingMedication(
    private val selector: MedicationSelector,
    private val termToFind: String,
    private val type: DrugInteraction.Type,
    private val labels: EvaluationLabels.Medication
) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val medications = record.medications ?: return medicationNotProvided(labels)
        val transporterInteractingMedicationActive =
            selector.activeWithInteraction(medications, termToFind, type, DrugInteraction.Group.TRANSPORTER).map { it.name }
        val transporterInteractingMedicationPlanned =
            selector.plannedWithInteraction(medications, termToFind, type, DrugInteraction.Group.TRANSPORTER).map { it.name }

        val typeText = type.name.lowercase()

        return when {
            transporterInteractingMedicationActive.isNotEmpty() -> {
                EvaluationFactory.recoverablePass(
                    labels.currentlyGetsTransporterInteractingMedicationRecoverablePass(
                        termToFind, typeText, concatLowercaseWithCommaAndAnd(transporterInteractingMedicationActive)
                    )
                )
            }

            transporterInteractingMedicationPlanned.isNotEmpty() -> {
                EvaluationFactory.warn(
                    labels.currentlyGetsTransporterInteractingMedicationWarn(
                        termToFind, typeText, concatLowercaseWithCommaAndAnd(transporterInteractingMedicationPlanned)
                    )
                )
            }

            else -> {
                EvaluationFactory.recoverableFail(
                    labels.currentlyGetsTransporterInteractingMedicationRecoverableFail(termToFind, typeText)
                )
            }
        }
    }
}