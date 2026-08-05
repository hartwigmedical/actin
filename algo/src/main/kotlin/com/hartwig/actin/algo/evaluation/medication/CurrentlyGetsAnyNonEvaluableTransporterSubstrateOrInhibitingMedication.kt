package com.hartwig.actin.algo.evaluation.medication

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.EvaluationLabels
import com.hartwig.actin.algo.evaluation.util.Format
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation

class CurrentlyGetsAnyNonEvaluableTransporterSubstrateOrInhibitingMedication(
    private val selector: MedicationSelector,
    private val types: List<String>,
    private val labels: EvaluationLabels.Medication
) :
    EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val medications = record.medications ?: return medicationNotProvided(labels)
        val hasActiveOrPlannedMedication = medications.any(selector::isActive) || medications.any(selector::isPlanned)
        val concatenatedTypes = Format.concatWithCommaAndOr(types)

        return when {
            medications.isEmpty() -> {
                EvaluationFactory.recoverableFail(
                    labels.currentlyGetsAnyNonEvaluableTransporterSubstrateOrInhibitingMedicationRecoverableFailNoMedication(
                        concatenatedTypes
                    )
                )
            }

            !hasActiveOrPlannedMedication -> {
                EvaluationFactory.recoverableFail(
                    labels.currentlyGetsAnyNonEvaluableTransporterSubstrateOrInhibitingMedicationRecoverableFailNoActiveOrPlanned(
                        concatenatedTypes
                    )
                )
            }

            else -> {
                EvaluationFactory.warn(
                    labels.currentlyGetsAnyNonEvaluableTransporterSubstrateOrInhibitingMedicationWarn(concatenatedTypes)
                )
            }
        }
    }
}