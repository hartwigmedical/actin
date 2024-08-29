package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.clinical.datamodel.AtcLevel
import com.hartwig.actin.clinical.datamodel.treatment.TreatmentCategory

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
                ((it.allLevels() intersect atcLevelsToFind).isNotEmpty() && !(it.treatment?.category?.equals(categoryToIgnore)
                    ?: false)) || it.isTrialMedication
            } ?: emptyList()

        return if (treatmentHistory.isEmpty() && priorCancerMedication.isEmpty()) {
            EvaluationFactory.fail("Patient has not had any prior cancer treatments", "Has not had any cancer treatment")
        } else {
            val categoryDisplay = categoryToIgnore?.let { "other than ${categoryToIgnore.display()} " } ?: ""
            val treatmentDisplay = "(treatment(s): ${treatmentHistory.joinToString(", ") { it.treatmentDisplay() }})"
            EvaluationFactory.pass(
                "Patient has had prior cancer treatment $categoryDisplay$treatmentDisplay", "Has received prior cancer treatment(s)"
            )
        }
    }
}