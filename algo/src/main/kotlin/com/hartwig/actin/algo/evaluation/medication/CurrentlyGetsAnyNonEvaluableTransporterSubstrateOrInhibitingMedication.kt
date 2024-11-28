package com.hartwig.actin.algo.evaluation.medication

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.util.Format.concatWithCommaAndOr
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation

class CurrentlyGetsAnyNonEvaluableTransporterSubstrateOrInhibitingMedication(private val selector: MedicationSelector, private val types: List<String>) :
    EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val medications = record.medications ?: return MEDICATION_NOT_PROVIDED
        val hasActiveOrPlannedMedication = medications.any(selector::isActive) || medications.any(selector::isPlanned)
        val concatenatedTypes = concatWithCommaAndOr(types)

        return when {
            medications.isEmpty() -> {
                EvaluationFactory.recoverableFail(
                    "Patient has empty medication list hence does not currently receive $concatenatedTypes substrate or inhibiting medication",
                    "No current $concatenatedTypes substrate or inhibiting medication use (no medication use)"
                )
            }

            !hasActiveOrPlannedMedication -> {
                EvaluationFactory.recoverableFail(
                    "Patient has no active or planned medication hence does not currently receive $concatenatedTypes substrate or inhibiting medication",
                    "No current $concatenatedTypes substrate or inhibiting medication use (no planned or active medication)"
                )
            }

            else -> {
                EvaluationFactory.warn(
                    "Currently undetermined if patient may use $concatenatedTypes substrate or inhibiting medication",
                    "Undetermined if patient may use $concatenatedTypes substrate or inhibiting medication"
                )
            }
        }
    }
}