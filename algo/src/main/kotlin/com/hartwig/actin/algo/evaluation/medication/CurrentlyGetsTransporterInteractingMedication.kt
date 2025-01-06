package com.hartwig.actin.algo.evaluation.medication

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.util.Format
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.DrugInteraction

class CurrentlyGetsTransporterInteractingMedication(
    private val selector: MedicationSelector,
    private val termToFind: String,
    private val type: DrugInteraction.Type
) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val medications = record.medications ?: return MEDICATION_NOT_PROVIDED
        val transporterInteractingMedicationActive =
            selector.activeWithInteraction(medications, termToFind, type, DrugInteraction.Group.TRANSPORTER).map { it.name }
        val transporterInteractingMedicationPlanned =
            selector.plannedWithInteraction(medications, termToFind, type, DrugInteraction.Group.TRANSPORTER).map { it.name }

        val typeText = type.name.lowercase()

        return when {
            transporterInteractingMedicationActive.isNotEmpty() -> {
                EvaluationFactory.recoverablePass(
                    "Active $termToFind $typeText medication use: ${
                        Format.concatLowercaseWithAnd(
                            transporterInteractingMedicationActive
                        )
                    }"
                )
            }

            transporterInteractingMedicationPlanned.isNotEmpty() -> {
                EvaluationFactory.recoverableWarn(
                    "Planned $termToFind $typeText medication use: ${
                        Format.concatLowercaseWithAnd(
                            transporterInteractingMedicationPlanned
                        )
                    }"
                )
            }

            else -> {
                EvaluationFactory.recoverableFail(
                    "No current $termToFind $typeText medication use"
                )
            }
        }
    }
}