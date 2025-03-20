package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.treatment.DrugType.Companion.RAS_MEK_MAPK_DIRECTLY_TARGETING_DRUG_SET
import com.hartwig.actin.datamodel.clinical.treatment.DrugType.Companion.RAS_MEK_MAPK_INDIRECTLY_TARGETING_DRUG_SET
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentCategory

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
                EvaluationFactory.pass("Has had targeted therapy $interferenceMessage ($treatmentDisplay)")
            }

            indirectPathwayInhibitionTreatments.isNotEmpty() -> {
                val treatmentDisplay = indirectPathwayInhibitionTreatments.joinToString(", ") { it.treatmentDisplay() }
                EvaluationFactory.warn("Has had targeted therapy ($treatmentDisplay) indirectly $interferenceMessage")
            }

            record.oncologicalHistory.any { TrialFunctions.treatmentMayMatchAsTrial(it, setOf(TreatmentCategory.TARGETED_THERAPY)) } -> {
                EvaluationFactory.undetermined("Has had trial drug - $undeterminedInterferenceMessage")
            }

            else -> {
                EvaluationFactory.fail("Has not received targeted therapy $interferenceMessage")
            }
        }
    }
}