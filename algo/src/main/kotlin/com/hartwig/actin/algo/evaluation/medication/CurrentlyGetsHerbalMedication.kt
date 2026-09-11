package com.hartwig.actin.algo.evaluation.medication

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation

class CurrentlyGetsHerbalMedication(private val selector: MedicationSelector) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val medications = record.medications ?: return MEDICATION_NOT_PROVIDED
        val hasActiveSelfCareMedication = medications.filter { it.isSelfCare }.any(selector::isActive)
        val hasPlannedSelfCareMedication = medications.filter { it.isSelfCare }.any(selector::isPlanned)

        return when {
            hasActiveSelfCareMedication -> {
                EvaluationFactory.undetermined("Undetermined if active herbal medications based on provided medications (self care medication)")
            }

            hasPlannedSelfCareMedication -> {
                EvaluationFactory.undetermined(
                    "Undetermined if planned herbal medications based on provided medications (planned self care medication)"
                )
            }

            else -> {
                EvaluationFactory.fail("No active herbal medications in provided medications")
            }
        }
    }
}