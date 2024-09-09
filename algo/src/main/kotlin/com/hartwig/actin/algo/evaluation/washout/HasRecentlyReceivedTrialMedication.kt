package com.hartwig.actin.algo.evaluation.washout

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.medication.MEDICATION_NOT_PROVIDED
import com.hartwig.actin.algo.evaluation.medication.MedicationSelector
import com.hartwig.actin.algo.evaluation.treatment.TreatmentSinceDateFunctions
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.Medication
import java.time.LocalDate

class HasRecentlyReceivedTrialMedication(
    private val selector: MedicationSelector, private val minStopDate: LocalDate
) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val medications = record.medications ?: return MEDICATION_NOT_PROVIDED
        if (minStopDate.isBefore(record.patient.registrationDate)) {
            return EvaluationFactory.undetermined(
                "Required stop date prior to registration date for recent trial medication usage evaluation",
                "Undetermined recent trial medication"
            )
        }

        val hasActiveOrRecentlyStoppedMedication =
            selector.activeOrRecentlyStopped(medications, minStopDate).any(Medication::isTrialMedication)

        val hadRecentTrialTreatment =
            record.oncologicalHistory.any { it.isTrial && TreatmentSinceDateFunctions.treatmentSinceMinDate(it, minStopDate, true) }

        return if (hasActiveOrRecentlyStoppedMedication || hadRecentTrialTreatment) {
            EvaluationFactory.pass(
                "Patient recently received trial medication - pay attention to washout period",
                "Recent trial medication - pay attention to washout period"
            )
        } else {
            EvaluationFactory.fail(
                "Patient has not recently received trial medication",
                "No recent trial medication"
            )
        }
    }
}