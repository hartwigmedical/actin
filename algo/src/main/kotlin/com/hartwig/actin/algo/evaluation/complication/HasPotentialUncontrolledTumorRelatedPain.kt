package com.hartwig.actin.algo.evaluation.complication

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.medication.MEDICATION_NOT_PROVIDED
import com.hartwig.actin.algo.evaluation.medication.MedicationSelector
import com.hartwig.actin.algo.evaluation.util.Format.concatLowercaseWithAnd
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.AtcLevel

private const val SEVERE_PAIN_COMPLICATION = "pain"

class HasPotentialUncontrolledTumorRelatedPain(private val selector: MedicationSelector, private val severePainMedication: Set<AtcLevel>) :
    EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val painComplications = ComplicationFunctions.findComplicationNamesMatchingAnyCategory(record, listOf(SEVERE_PAIN_COMPLICATION))
        if (painComplications.isNotEmpty()) {
            return EvaluationFactory.pass(
                "Patient has complication related to pain: " + concatLowercaseWithAnd(painComplications) +
                        ", potentially indicating uncontrolled tumor related pain",
                "Present " + concatLowercaseWithAnd(painComplications)
            )
        }
        val medications = record.medications ?: return MEDICATION_NOT_PROVIDED
        val (activePainMedications, plannedPainMedications) = selector.extractActiveAndPlannedWithCategory(
            medications,
            severePainMedication
        )

        return when {
            activePainMedications.isNotEmpty() -> {
                EvaluationFactory.pass(
                    "Patient receives pain medication: " + concatLowercaseWithAnd(activePainMedications) +
                            ", potentially indicating uncontrolled tumor related pain",
                    "Receives " + concatLowercaseWithAnd(activePainMedications) + " indicating pain "
                )
            }

            plannedPainMedications.isNotEmpty() -> {
                EvaluationFactory.warn(
                    "Patient plans to receive pain medication: " + concatLowercaseWithAnd(plannedPainMedications) +
                            ", potentially indicating uncontrolled tumor related pain",
                    "Plans to receive " + concatLowercaseWithAnd(plannedPainMedications) + " indicating pain"
                )
            }

            else ->
                EvaluationFactory.fail(
                    "Patient does not have uncontrolled tumor related pain",
                    "No potential uncontrolled tumor related pain"
                )
        }
    }
}