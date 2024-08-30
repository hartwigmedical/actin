package com.hartwig.actin.algo.evaluation.medication

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.util.Format.concat
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation

class CurrentlyGetsMedicationOfName(private val selector: MedicationSelector, private val termsToFind: Set<String>) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val medications = record.medications ?: return MEDICATION_NOT_PROVIDED
        val hasActiveMedicationWithName = selector.activeWithAnyTermInName(medications, termsToFind).isNotEmpty()
        val hasPlannedMedicationWithName = selector.plannedWithAnyTermInName(medications, termsToFind).isNotEmpty()

        return when {
            hasActiveMedicationWithName -> {
                EvaluationFactory.recoverablePass(
                    "Patient currently gets medication with name " + concat(termsToFind),
                    concat(termsToFind) + " medication use"
                )
            }

            hasPlannedMedicationWithName -> {
                EvaluationFactory.recoverableWarn(
                    "Patient plans to get medication with name " + concat(termsToFind),
                    "Planned " + concat(termsToFind) + " medication use"
                )
            }

            else -> {
                EvaluationFactory.recoverableFail(
                    "Patient currently does not get medication with name " + concat(termsToFind),
                    "No " + concat(termsToFind) + " medication use"
                )
            }
        }
    }
}