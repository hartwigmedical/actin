package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.clinical.datamodel.treatment.TreatmentCategory

class HasHadAnyCancerTreatment(private val categoryToIgnore: TreatmentCategory?) : EvaluationFunction {
    override fun evaluate(record: PatientRecord): Evaluation {
        val treatmentHistory =
            if (categoryToIgnore == null) {
                record.oncologicalHistory
            } else {
                record.oncologicalHistory.filterNot { it.categories().contains(categoryToIgnore) }
            }

        return if (treatmentHistory.isEmpty()) {
            EvaluationFactory.fail("Patient has not had any prior cancer treatments", "Has not had any cancer treatment")
        } else {
            val categoryDisplay = categoryToIgnore?.let { "other than ${categoryToIgnore.display()} " } ?: ""
            val treatmentDisplay = "${categoryDisplay}(treatment(s): ${treatmentHistory.joinToString(", ") { it.treatmentDisplay() }})"
            EvaluationFactory.pass(
                    "Patient has had prior cancer treatment $treatmentDisplay",
                    "Had received prior cancer treatment(s)"
                )
        }
    }
}