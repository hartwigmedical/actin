package com.hartwig.actin.algo.evaluation.washout

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.EvaluationLabels
import com.hartwig.actin.algo.evaluation.medication.MedicationSelector
import com.hartwig.actin.algo.evaluation.medication.medicationNotProvided
import com.hartwig.actin.algo.evaluation.treatment.TreatmentVersusDateFunctions
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.Medication
import java.time.LocalDate

class HasRecentlyReceivedTrialMedication(
    private val selector: MedicationSelector,
    private val minStopDate: LocalDate,
    private val washoutLabels: EvaluationLabels.Washout,
    private val medicationLabels: EvaluationLabels.Medication
) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        if (minStopDate.isBefore(record.patient.registrationDate)) {
            return EvaluationFactory.undetermined(washoutLabels.hasRecentlyReceivedTrialMedicationUndeterminedRegistration())
        }

        val hadRecentTrialTreatment =
            record.oncologicalHistory.any { it.isTrial && TreatmentVersusDateFunctions.treatmentSinceMinDate(it, minStopDate, false) }

        val hadTrialTreatmentWithUnknownDate =
            record.oncologicalHistory.any { it.isTrial && TreatmentVersusDateFunctions.treatmentSinceMinDate(it, minStopDate, true) }

        if (!(hadRecentTrialTreatment || hadTrialTreatmentWithUnknownDate) && record.medications == null) {
            return medicationNotProvided(medicationLabels)
        }

        val hasActiveOrRecentlyStoppedTrialMedication =
            selector.activeOrRecentlyStopped(record.medications ?: emptyList(), minStopDate).any(Medication::isTrialMedication)

        return when {
            hasActiveOrRecentlyStoppedTrialMedication || hadRecentTrialTreatment -> {
                EvaluationFactory.pass(washoutLabels.hasRecentlyReceivedTrialMedicationPass())
            }

            hadTrialTreatmentWithUnknownDate -> {
                EvaluationFactory.undetermined(washoutLabels.hasRecentlyReceivedTrialMedicationUndeterminedUnknownDate())
            }

            else -> {
                EvaluationFactory.fail(washoutLabels.hasRecentlyReceivedTrialMedicationFail())
            }
        }
    }
}