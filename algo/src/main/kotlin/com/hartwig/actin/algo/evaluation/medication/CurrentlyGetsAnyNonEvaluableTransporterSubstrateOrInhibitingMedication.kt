package com.hartwig.actin.algo.evaluation.medication

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.util.Format
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation

class CurrentlyGetsAnyNonEvaluableTransporterSubstrateOrInhibitingMedication(
    private val selector: MedicationSelector,
    private val types: List<String>
) :
    EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val medications = record.medications ?: return MEDICATION_NOT_PROVIDED
        val hasActiveOrPlannedMedication = medications.any(selector::isActive) || medications.any(selector::isPlanned)
        val concatenatedTypes = Format.concatWithCommaAndOr(types)

        return when {
            medications.isEmpty() -> {
                EvaluationFactory.recoverableFail(
                    "No current $concatenatedTypes substrate or inhibiting medication use (no medication use)"
                )
            }

            !hasActiveOrPlannedMedication -> {
                EvaluationFactory.recoverableFail(
                    "No current $concatenatedTypes substrate or inhibiting medication use (no planned or active medication)"
                )
            }

            else -> {
                EvaluationFactory.warn(
                    "Undetermined if patient uses $concatenatedTypes substrate or inhibiting medication"
                )
            }
        }
    }
}