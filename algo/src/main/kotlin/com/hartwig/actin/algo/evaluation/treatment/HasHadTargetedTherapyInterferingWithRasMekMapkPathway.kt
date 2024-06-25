package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.clinical.datamodel.treatment.DrugType.Companion.rasMekMapkDirectlyTargetingDrugSet
import com.hartwig.actin.clinical.datamodel.treatment.DrugType.Companion.rasMekMapkIndirectlyTargetingDrugSet
import com.hartwig.actin.clinical.datamodel.treatment.TreatmentCategory

class HasHadTargetedTherapyInterferingWithRasMekMapkPathway : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val treatmentSummary = TreatmentSummaryForCategory.createForTreatmentHistory(
            record.oncologicalHistory, TreatmentCategory.TARGETED_THERAPY
        )

        val hasHadDirectPathwayInhibition = treatmentSummary.hasSpecificMatch()
                && treatmentSummary.specificMatches.any { it.matchesTypeFromSet(rasMekMapkDirectlyTargetingDrugSet) == true }
        val hasHadIndirectPathwayInhibition = treatmentSummary.hasSpecificMatch()
                && treatmentSummary.specificMatches.any { it.matchesTypeFromSet(rasMekMapkIndirectlyTargetingDrugSet) == true }
        val messageEnding = "undetermined interference with RAS/MEK/MAPK pathway"
        val interferenceMessage = "interfering with RAS/MEK/MAPK pathway"

        return when {
            hasHadDirectPathwayInhibition -> {
                val treatmentDisplay = treatmentSummary.specificMatches.joinToString(", ") { it.treatmentDisplay() }
                val message = "targeted therapy $interferenceMessage ($treatmentDisplay)"
                EvaluationFactory.pass("Patient has received $message", "Has had $message")
            }

            hasHadIndirectPathwayInhibition -> {
                val treatmentDisplay = record.oncologicalHistory
                    .filter { it.categories().contains(TreatmentCategory.TARGETED_THERAPY) }
                    .joinToString(", ") { it.treatmentDisplay() }
                val message = "targeted therapy ($treatmentDisplay) - $messageEnding"
                EvaluationFactory.undetermined("Patient has received $message", "Has had $message")
            }

            treatmentSummary.hasPossibleTrialMatch() -> {
                val message = "trial drug - $messageEnding"
                EvaluationFactory.undetermined("Patient has received $message", "Has had $message")
            }

            else -> {
                EvaluationFactory.fail(
                    "Patient did not receive targeted therapy $interferenceMessage",
                    "Has not received targeted therapy $interferenceMessage"
                )
            }
        }
    }
}