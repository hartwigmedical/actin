package com.hartwig.actin.algo.evaluation.medication

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.EvaluationLabels
import com.hartwig.actin.algo.evaluation.util.Format.concatLowercaseWithCommaAndOr
import com.hartwig.actin.algo.evaluation.util.Format.concatWithCommaAndOr
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation

class CurrentlyGetsMedicationOfName(
    private val selector: MedicationSelector,
    private val termsToFind: Set<String>,
    private val labels: EvaluationLabels.Medication
) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val medications = record.medications ?: return medicationNotProvided(labels)
        val hasActiveMedicationWithName = selector.activeWithAnyTermInName(medications, termsToFind).isNotEmpty()
        val hasPlannedMedicationWithName = selector.plannedWithAnyTermInName(medications, termsToFind).isNotEmpty()

        return when {
            hasActiveMedicationWithName -> {
                EvaluationFactory.recoverablePass(labels.currentlyGetsMedicationOfNameRecoverablePass(concatWithCommaAndOr(termsToFind)))
            }

            hasPlannedMedicationWithName -> {
                EvaluationFactory.warn(labels.currentlyGetsMedicationOfNameWarn(concatLowercaseWithCommaAndOr(termsToFind)))
            }

            else -> {
                EvaluationFactory.recoverableFail(
                    labels.currentlyGetsMedicationOfNameRecoverableFail(concatLowercaseWithCommaAndOr(termsToFind))
                )
            }
        }
    }
}