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
        if (minStopDate.isBefore(record.patient.registrationDate)) {
            return EvaluationFactory.undetermined("Recent trial medication undetermined (required stop date prior to registration date)")
        }

        val hadRecentTrialTreatment =
            record.oncologicalHistory.any { it.isTrial && TreatmentSinceDateFunctions.treatmentSinceMinDate(it, minStopDate, false) }

        val hadTrialTreatmentWithUnknownDate =
            record.oncologicalHistory.any { it.isTrial && TreatmentSinceDateFunctions.treatmentSinceMinDate(it, minStopDate, true) }

        if (!(hadRecentTrialTreatment || hadTrialTreatmentWithUnknownDate) && record.medications == null) {
            return MEDICATION_NOT_PROVIDED
        }

        val hasActiveOrRecentlyStoppedTrialMedication =
            selector.activeOrRecentlyStopped(record.medications ?: emptyList(), minStopDate).any(Medication::isTrialMedication)

        return when {
            hasActiveOrRecentlyStoppedTrialMedication || hadRecentTrialTreatment -> {
                EvaluationFactory.pass("Recent trial medication - pay attention to washout period")
            }

            hadTrialTreatmentWithUnknownDate -> {
                EvaluationFactory.undetermined("Received trial medication but date unknown")
            }

            else -> {
                EvaluationFactory.fail("No recent trial medication")
            }
        }
    }
}