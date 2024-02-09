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
                record.clinical.oncologicalHistory
            } else {
                record.clinical.oncologicalHistory.filterNot { it.categories().contains(categoryToIgnore) }
            }

        return if (treatmentHistory.isEmpty()) {
            EvaluationFactory.fail("Patient has not had any prior cancer treatments", "Has not had any cancer treatment")
        } else {
            val treatmentDisplay = "(treatment(s): ${treatmentHistory.joinToString(", ") { it.treatmentDisplay() }})"
            if (categoryToIgnore == null) {
                EvaluationFactory.pass(
                    "Patient has had prior cancer treatment $treatmentDisplay",
                    "Had had any cancer treatment $treatmentDisplay"
                )
            } else {
                EvaluationFactory.pass(
                    "Patient has had prior cancer treatment other than ${categoryToIgnore.display()} $treatmentDisplay",
                    "Had had any cancer treatment other than ${categoryToIgnore.display()} $treatmentDisplay"
                )
            }
        }
    }
}