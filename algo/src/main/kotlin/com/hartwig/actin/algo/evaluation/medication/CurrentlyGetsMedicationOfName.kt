package com.hartwig.actin.algo.evaluation.medication

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.util.Format.concat

class CurrentlyGetsMedicationOfName(private val selector: MedicationSelector, private val termsToFind: Set<String>) : EvaluationFunction {
        
    override fun evaluate(record: PatientRecord): Evaluation {
        val hasActiveMedicationWithName = selector.activeWithAnyTermInName(record.medications, termsToFind).isNotEmpty()
        val hasPlannedMedicationWithName = selector.plannedWithAnyTermInName(record.medications, termsToFind).isNotEmpty()

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