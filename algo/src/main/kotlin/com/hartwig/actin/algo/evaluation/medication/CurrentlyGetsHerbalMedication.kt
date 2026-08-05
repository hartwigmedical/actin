package com.hartwig.actin.algo.evaluation.medication

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.EvaluationLabels
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation

class CurrentlyGetsHerbalMedication(private val selector: MedicationSelector, private val labels: EvaluationLabels.Medication) :
    EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val medications = record.medications ?: return medicationNotProvided(labels)
        val hasActiveSelfCareMedication = medications.filter { it.isSelfCare }.any(selector::isActive)
        val hasPlannedSelfCareMedication = medications.filter { it.isSelfCare }.any(selector::isPlanned)

        return when {
            hasActiveSelfCareMedication -> {
                EvaluationFactory.undetermined(labels.currentlyGetsHerbalMedicationUndeterminedActive())
            }

            hasPlannedSelfCareMedication -> {
                EvaluationFactory.undetermined(labels.currentlyGetsHerbalMedicationUndeterminedPlanned())
            }

            else -> {
                EvaluationFactory.fail(labels.currentlyGetsHerbalMedicationFail())
            }
        }
    }
}