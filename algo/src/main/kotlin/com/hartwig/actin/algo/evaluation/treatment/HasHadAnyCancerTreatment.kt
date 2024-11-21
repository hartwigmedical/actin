package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.treatment.MedicationFunctions.createTreatmentHistoryEntriesFromMedications
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.AtcLevel
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentCategory

class HasHadAnyCancerTreatment(private val categoryToIgnore: TreatmentCategory?, private val atcLevelsToFind: Set<AtcLevel>) :
    EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val effectiveTreatmentHistoryWithoutTrialMedication =
            record.oncologicalHistory + createTreatmentHistoryEntriesFromMedications(
                record.medications?.filter { (it.allLevels() intersect atcLevelsToFind).isNotEmpty() })

        val hasHadPriorCancerTreatment =
            if (categoryToIgnore == null) {
                effectiveTreatmentHistoryWithoutTrialMedication.isNotEmpty()
            } else {
                effectiveTreatmentHistoryWithoutTrialMedication.any { it.categories().any { category -> category != categoryToIgnore } }
            }

        val hasHadTrial =
            effectiveTreatmentHistoryWithoutTrialMedication.any { it.isTrial } || record.medications?.any { it.isTrialMedication } == true

        return when {
            hasHadPriorCancerTreatment -> {
                val categoryDisplay = categoryToIgnore?.let { " other than ${categoryToIgnore.display()} " } ?: ""
                EvaluationFactory.pass(
                    "Patient has had prior cancer treatment$categoryDisplay", "Has received prior cancer treatment(s)"
                )
            }

            hasHadTrial -> {
                EvaluationFactory.undetermined(
                    "Patient has participated in a trial, inconclusive if patient has had any cancer treatment",
                    "Inconclusive if patient had any prior cancer treatment due to trial participation"
                )
            }

            else -> {
                EvaluationFactory.fail("Patient has not had any prior cancer treatments", "Has not had any cancer treatment")
            }
        }
    }
}