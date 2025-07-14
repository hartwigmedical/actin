package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.medication.MedicationSelector
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.Medication
import java.time.LocalDate

class IsNotParticipatingInAnotherInterventionalTrial(
    private val selector: MedicationSelector,
    private val minStopDate: LocalDate
) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val hadRecentTrialTreatment =
            record.oncologicalHistory.any { it.isTrial && TreatmentVersusDateFunctions.treatmentSinceMinDate(it, minStopDate, false) }

        val hasActiveOrRecentlyStoppedTrialMedication =
            selector.activeOrRecentlyStopped(record.medications ?: emptyList(), minStopDate).any(Medication::isTrialMedication)

        return when {
            hadRecentTrialTreatment || hasActiveOrRecentlyStoppedTrialMedication -> {
                EvaluationFactory.warn("Recent trial treatment - undetermined if patient is participating in another interventional trial")
            }

            else -> EvaluationFactory.notEvaluated("Assumed that patient is not participating in another interventional trial")
        }
    }
}