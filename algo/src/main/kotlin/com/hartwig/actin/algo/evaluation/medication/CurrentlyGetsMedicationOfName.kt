package com.hartwig.actin.algo.evaluation.medication

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.util.Format.concatLowercaseWithCommaAndOr
import com.hartwig.actin.algo.evaluation.util.Format.concatWithCommaAndOr
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation

class CurrentlyGetsMedicationOfName(private val selector: MedicationSelector, private val termsToFind: Set<String>) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val medications = record.medications ?: return MEDICATION_NOT_PROVIDED
        val hasActiveMedicationWithName = selector.activeWithAnyTermInName(medications, termsToFind).isNotEmpty()
        val hasPlannedMedicationWithName = selector.plannedWithAnyTermInName(medications, termsToFind).isNotEmpty()

        return when {
            hasActiveMedicationWithName -> {
                EvaluationFactory.recoverablePass(concatWithCommaAndOr(termsToFind) + " medication use")
            }

            hasPlannedMedicationWithName -> {
                EvaluationFactory.recoverableWarn("Planned " + concatLowercaseWithCommaAndOr(termsToFind) + " medication use")
            }

            else -> {
                EvaluationFactory.recoverableFail("No " + concatLowercaseWithCommaAndOr(termsToFind) + " medication use")
            }
        }
    }
}