package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.EvaluationLabels
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.treatment.DrugType.Companion.NSCLC_SOC_TARGETED_THERAPY_DRUG_TYPES
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentCategory
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentType

class HasHadSOCTargetedTherapyForNSCLC(
    private val genesToIgnore: Set<String>,
    private val labels: EvaluationLabels.Treatment
) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val drugTypeSet = returnDrugTypeSet(genesToIgnore)
        val treatmentSummary = TreatmentSummaryForCategory.createForTreatmentHistory(
            record.oncologicalHistory,
            TreatmentCategory.TARGETED_THERAPY,
            { historyEntry -> historyEntry.matchesTypeFromSet(drugTypeSet) }
        )
        val matches = treatmentSummary.specificMatches.joinToString { it.treatmentDisplay() }

        return when {
            treatmentSummary.hasSpecificMatch() -> {
                EvaluationFactory.pass(labels.hasHadSOCTargetedTherapyForNSCLCPass(matches))
            }
            else -> {
                EvaluationFactory.fail(labels.hasHadSOCTargetedTherapyForNSCLCFail())
            }
        }
    }

    private fun returnDrugTypeSet(genesToIgnore: Set<String>): Set<TreatmentType> {
        return NSCLC_SOC_TARGETED_THERAPY_DRUG_TYPES.filterNot { it.key in genesToIgnore }.values.flatten().toSet()
    }
}
