package com.hartwig.actin.algo.evaluation.washout

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.medication.MEDICATION_NOT_PROVIDED
import com.hartwig.actin.algo.evaluation.medication.MedicationSelector
import com.hartwig.actin.algo.evaluation.util.DateComparison.isAfterDate
import com.hartwig.actin.algo.evaluation.util.Format.concatLowercaseWithAnd
import com.hartwig.actin.clinical.datamodel.Medication
import java.time.LocalDate

class HasRecentlyReceivedTrialMedication(
    private val selector: MedicationSelector, private val minStopDate: LocalDate
) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val medications = record.medications ?: return MEDICATION_NOT_PROVIDED
        if (minStopDate.isBefore(record.patient.registrationDate)) {
            return EvaluationFactory.undetermined(
                "Required stop date prior to registration date for recent trial medication usage evaluation",
                "Recent trial medication"
            )
        }

        val activeOrRecentlyStopped = selector.activeOrRecentlyStopped(medications, minStopDate).filter(Medication::isTrialMedication)

        val foundMedicationNames = activeOrRecentlyStopped.map { it.name }.filter { it.isNotEmpty() }

        val trialEntries = record.oncologicalHistory.filter { it.isTrial && isAfterDate(
            minStopDate,
            it.startYear,
            it.startMonth
        ) == true }

        return if (activeOrRecentlyStopped.isNotEmpty() || trialEntries.isNotEmpty()) {
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