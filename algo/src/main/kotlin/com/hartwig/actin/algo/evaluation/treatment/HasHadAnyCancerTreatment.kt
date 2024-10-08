package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.treatment.TreatmentFunctions.createTreatmentHistoryEntriesFromMedications
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentCategory

class HasHadAnyCancerTreatment(private val categoryToIgnore: TreatmentCategory?) :
    EvaluationFunction {
    override fun evaluate(record: PatientRecord): Evaluation {
        val effectiveTreatmentHistory = record.oncologicalHistory + createTreatmentHistoryEntriesFromMedications(record.medications)

        val hasHadPriorCancerTreatment =
            if (categoryToIgnore == null) {
                effectiveTreatmentHistory.isNotEmpty()
            } else {
                effectiveTreatmentHistory.any { it.categories().any { category -> category != categoryToIgnore } }
            }

        val hasHadTrial = effectiveTreatmentHistory.any { it.isTrial }

        return when {
            hasHadTrial -> {
                EvaluationFactory.undetermined(
                    "Patient has participated in a trial recently, inconclusive if patient has had any cancer treatment",
                    "Inconclusive if patient had any prior cancer treatment due to trial participation"
                )
            }

            !hasHadPriorCancerTreatment -> {
                EvaluationFactory.fail("Patient has not had any prior cancer treatments", "Has not had any cancer treatment")
            }

            else -> {
                val categoryDisplay = categoryToIgnore?.let { "other than ${categoryToIgnore.display()} " } ?: ""
                EvaluationFactory.pass(
                    "Patient has had prior cancer treatment $categoryDisplay", "Has received prior cancer treatment(s)"
                )
            }
        }
    }
}