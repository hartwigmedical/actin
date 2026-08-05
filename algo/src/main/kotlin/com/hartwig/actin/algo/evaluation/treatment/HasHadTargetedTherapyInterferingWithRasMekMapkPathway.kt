package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.EvaluationLabels
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.treatment.DrugType.Companion.RAS_MEK_MAPK_DIRECTLY_TARGETING_DRUG_SET
import com.hartwig.actin.datamodel.clinical.treatment.DrugType.Companion.RAS_MEK_MAPK_INDIRECTLY_TARGETING_DRUG_SET
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentCategory

class HasHadTargetedTherapyInterferingWithRasMekMapkPathway(private val labels: EvaluationLabels.Treatment) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val directPathwayInhibitionTreatments = record.oncologicalHistory.filter { it.matchesTypeFromSet(
            RAS_MEK_MAPK_DIRECTLY_TARGETING_DRUG_SET) == true }
        val indirectPathwayInhibitionTreatments = record.oncologicalHistory.filter { it.matchesTypeFromSet(
            RAS_MEK_MAPK_INDIRECTLY_TARGETING_DRUG_SET) == true }

        return when {
            directPathwayInhibitionTreatments.isNotEmpty() -> {
                val treatmentDisplay = directPathwayInhibitionTreatments.map { it.treatmentDisplay() }
                EvaluationFactory.pass(labels.hasHadTargetedTherapyInterferingWithRasMekMapkPathwayPass(treatmentDisplay))
            }

            indirectPathwayInhibitionTreatments.isNotEmpty() -> {
                val treatmentDisplay = indirectPathwayInhibitionTreatments.map { it.treatmentDisplay() }
                EvaluationFactory.warn(labels.hasHadTargetedTherapyInterferingWithRasMekMapkPathwayWarn(treatmentDisplay))
            }

            record.oncologicalHistory.any { TrialFunctions.treatmentMayMatchAsTrial(it, setOf(TreatmentCategory.TARGETED_THERAPY)) } -> {
                EvaluationFactory.undetermined(labels.hasHadTargetedTherapyInterferingWithRasMekMapkPathwayUndetermined())
            }

            else -> {
                EvaluationFactory.fail(labels.hasHadTargetedTherapyInterferingWithRasMekMapkPathwayFail())
            }
        }
    }
}