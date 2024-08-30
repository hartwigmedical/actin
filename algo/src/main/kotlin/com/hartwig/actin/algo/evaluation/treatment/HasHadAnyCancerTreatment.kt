package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.AtcLevel
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentCategory

class HasHadAnyCancerTreatment(private val categoryToIgnore: TreatmentCategory?, private val atcLevelsToFind: Set<AtcLevel>) :
    EvaluationFunction {
    override fun evaluate(record: PatientRecord): Evaluation {
        val treatmentHistory =
            if (categoryToIgnore == null) {
                record.oncologicalHistory
            } else {
                record.oncologicalHistory.filterNot { it.categories().contains(categoryToIgnore) }
            }

        val priorCancerMedication = record.medications
            ?.filter {
                ((it.allLevels() intersect atcLevelsToFind).isNotEmpty() && !(it.drug?.category?.equals(categoryToIgnore)
                    ?: false)) || it.isTrialMedication
            } ?: emptyList()

        return if (treatmentHistory.isEmpty() && priorCancerMedication.isEmpty()) {
            EvaluationFactory.fail("Patient has not had any prior cancer treatments", "Has not had any cancer treatment")
        } else {
            val categoryDisplay = categoryToIgnore?.let { "other than ${categoryToIgnore.display()} " } ?: ""
            EvaluationFactory.pass(
                "Patient has had prior cancer treatment $categoryDisplay", "Has received prior cancer treatment(s)"
            )
        }
    }
}