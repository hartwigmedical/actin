package com.hartwig.actin.algo.evaluation.washout

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.medication.MedicationSelector
import com.hartwig.actin.algo.evaluation.medication.medicationWhenProvidedEvaluation
import com.hartwig.actin.algo.evaluation.util.Format.concatLowercaseWithAnd
import com.hartwig.actin.clinical.datamodel.Medication
import java.time.LocalDate

class HasRecentlyReceivedTrialMedication(
    private val selector: MedicationSelector, private val minStopDate: LocalDate
) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        return medicationWhenProvidedEvaluation(record) { medications ->
            if (minStopDate.isBefore(record.patient.registrationDate)) {
                return@medicationWhenProvidedEvaluation EvaluationFactory.undetermined(
                    "Required stop date prior to registration date for recent trial medication usage evaluation",
                    "Recent trial medication"
                )
            }

            val activeOrRecentlyStopped = selector.activeOrRecentlyStopped(medications, minStopDate).filter(Medication::isTrialMedication)

            val foundMedicationNames = activeOrRecentlyStopped.map { it.name }.filter { it.isNotEmpty() }

            if (activeOrRecentlyStopped.isNotEmpty()) {
                val foundMedicationString =
                    if (foundMedicationNames.isNotEmpty()) ": ${concatLowercaseWithAnd(foundMedicationNames)}" else ""
                EvaluationFactory.pass(
                    "Patient recently received trial medication $foundMedicationString - pay attention to washout period",
                    "Recent trial medication $foundMedicationString - pay attention to washout period"
                )
            } else {
                EvaluationFactory.fail(
                    "Patient has not recently received trial medication",
                    "No recent trial medication"
                )
            }
        }
    }
}