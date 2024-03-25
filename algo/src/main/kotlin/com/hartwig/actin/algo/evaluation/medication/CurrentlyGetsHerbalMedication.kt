package com.hartwig.actin.algo.evaluation.medication

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction

class CurrentlyGetsHerbalMedication(private val selector: MedicationSelector) : EvaluationFunction {
    override fun evaluate(record: PatientRecord): Evaluation {
        val medications = record.medications ?: return MEDICATION_NOT_PROVIDED
        val hasActiveSelfCareMedication = medications.filter { it.isSelfCare }.any(selector::isActive)

        val hasPlannedSelfCareMedication = medications.filter { it.isSelfCare }.any(selector::isPlanned)

        return when {
            hasActiveSelfCareMedication -> {
                EvaluationFactory.undetermined(
                    "Patient uses self care medication however undetermined if patient may use herbal medications",
                    "Undetermined if patient may use herbal medications"
                )
            }

            hasPlannedSelfCareMedication -> {
                EvaluationFactory.undetermined(
                    "Patient plans to use self care medication however undetermined if patient may plan to use herbal medications",
                    "Undetermined if patient may plan to use herbal medications"
                )
            }

            else -> {
                EvaluationFactory.fail(
                    "Patient does not use or plan to use self care medication hence won't use herbal medications",
                    "No use of herbal medications"
                )
            }
        }
    }
}