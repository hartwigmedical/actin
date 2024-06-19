package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.clinical.datamodel.treatment.TreatmentCategory

class HasHadTargetedTherapyInterferingWithRasMekMapkPathway : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val treatmentSummary = TreatmentSummaryForCategory.createForTreatmentHistory(
            record.oncologicalHistory, TreatmentCategory.TARGETED_THERAPY
        )
        return when {
            treatmentSummary.hasSpecificMatch() || treatmentSummary.hasApproximateMatch() || treatmentSummary.hasPossibleTrialMatch() -> {
                val treatmentDisplay = record.oncologicalHistory
                    .filter { it.categories().contains(TreatmentCategory.TARGETED_THERAPY) }
                    .joinToString(", ") { it.treatmentDisplay() }
                val messageEnding = "targeted therapy ($treatmentDisplay) but undetermined interference with RAS/MEK/MAPK pathway"
                EvaluationFactory.undetermined("Patient has received $messageEnding", "Has had $messageEnding")
            } else -> {
                EvaluationFactory.fail("Patient did not receive targeted therapy", "Has not received targeted therapy")
            }
        }
    }
}