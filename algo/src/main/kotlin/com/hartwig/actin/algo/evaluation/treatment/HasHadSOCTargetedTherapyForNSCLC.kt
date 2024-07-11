package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.clinical.datamodel.treatment.DrugType.Companion.NSCLC_SOC_TARGETED_THERAPY_DRUG_TYPES
import com.hartwig.actin.clinical.datamodel.treatment.TreatmentCategory
import com.hartwig.actin.clinical.datamodel.treatment.TreatmentType

class HasHadSOCTargetedTherapyForNSCLC(private val genesToIgnore: List<String>) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val drugTypeSet = returnDrugTypeSet(genesToIgnore)
        val treatmentSummary = TreatmentSummaryForCategory.createForTreatmentHistory(
            record.oncologicalHistory,
            TreatmentCategory.TARGETED_THERAPY,
            { historyEntry -> historyEntry.matchesTypeFromSet(drugTypeSet) }
        )
        val matches = treatmentSummary.specificMatches.joinToString { it.treatmentDisplay()}

        return when {
            treatmentSummary.hasSpecificMatch() -> {
                EvaluationFactory.pass(
                    "Patient has received standard of care targeted therapy for NSCLC ($matches)",
                    "Has received SOC targeted therapy for NSCLC ($matches)"
                )
            }
            else -> {
                EvaluationFactory.fail(
                    "Patient has not received standard of care targeted therapy for NSCLC",
                    "Has not received SOC targeted therapy for NSCLC"
                )
            }
        }
    }

    private fun returnDrugTypeSet(genesToIgnore: List<String>): Set<TreatmentType> {
        return NSCLC_SOC_TARGETED_THERAPY_DRUG_TYPES.filterNot { it.key in genesToIgnore }.values.flatten().toSet()
    }

    companion object {

    }
}