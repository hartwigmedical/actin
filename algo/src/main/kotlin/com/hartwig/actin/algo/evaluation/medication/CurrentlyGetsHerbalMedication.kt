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
                EvaluationFactory.undetermined("Undetermined if patient may use herbal medications (self care medication use)")
            }

            hasPlannedSelfCareMedication -> {
                EvaluationFactory.undetermined("Undetermined if patient may plan to use herbal medications (planned self care medication use)")
            }

            else -> {
                EvaluationFactory.fail("No use of herbal medications (no self care medication use)")
            }
        }
    }
}