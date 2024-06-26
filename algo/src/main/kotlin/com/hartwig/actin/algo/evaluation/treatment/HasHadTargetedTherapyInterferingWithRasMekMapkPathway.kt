package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.clinical.datamodel.treatment.DrugType.Companion.RAS_MEK_MAPK_DIRECTLY_TARGETING_DRUG_SET
import com.hartwig.actin.clinical.datamodel.treatment.DrugType.Companion.RAS_MEK_MAPK_INDIRECTLY_TARGETING_DRUG_SET
import com.hartwig.actin.clinical.datamodel.treatment.TreatmentCategory

class HasHadTargetedTherapyInterferingWithRasMekMapkPathway : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val directPathwayInhibitionTreatments = record.oncologicalHistory.filter { it.matchesTypeFromSet(
            RAS_MEK_MAPK_DIRECTLY_TARGETING_DRUG_SET) == true }
        val indirectPathwayInhibitionTreatments = record.oncologicalHistory.filter { it.matchesTypeFromSet(
            RAS_MEK_MAPK_INDIRECTLY_TARGETING_DRUG_SET) == true }
        val undeterminedInterferenceMessage = "undetermined interference with RAS/MEK/MAPK pathway"
        val interferenceMessage = "interfering with RAS/MEK/MAPK pathway"

        return when {
            directPathwayInhibitionTreatments.isNotEmpty() -> {
                val treatmentDisplay = directPathwayInhibitionTreatments.joinToString(", ") { it.treatmentDisplay() }
                val message = "targeted therapy $interferenceMessage ($treatmentDisplay)"
                EvaluationFactory.pass("Patient has received $message", "Has had $message")
            }

            indirectPathwayInhibitionTreatments.isNotEmpty() -> {
                val treatmentDisplay = indirectPathwayInhibitionTreatments.joinToString(", ") { it.treatmentDisplay() }
                val message = "targeted therapy ($treatmentDisplay) - indirectly $interferenceMessage"
                EvaluationFactory.warn("Patient has received $message", "Has had $message")
            }

            record.oncologicalHistory.any { TrialFunctions.treatmentMayMatchAsTrial(it, TreatmentCategory.TARGETED_THERAPY) } -> {
                val message = "trial drug - $undeterminedInterferenceMessage"
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