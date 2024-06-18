package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.clinical.datamodel.treatment.DrugType
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
        val matches = treatmentSummary.specificMatches.joinToString { it.treatmentDisplay() }

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
        val NSCLC_SOC_TARGETED_THERAPY_DRUG_TYPES = mapOf(
            "ALK" to setOf(DrugType.ALK_INHIBITOR,
                DrugType.ALK_TYROSINE_KINASE_INHIBITOR,
                DrugType.ALK_INHIBITOR_GEN_1,
                DrugType.ALK_INHIBITOR_GEN_2,
                DrugType.ALK_INHIBITOR_GEN_3
            ),
            "EGFR" to setOf(
                DrugType.EGFR_INHIBITOR,
                DrugType.EGFR_EXON_20_INS_TARGETED_THERAPY,
                DrugType.EGFR_C797X_TKI,
                DrugType.EGFR_INHIBITOR_GEN_1,
                DrugType.EGFR_INHIBITOR_GEN_2,
                DrugType.EGFR_INHIBITOR_GEN_3,
                DrugType.EGFR_EXON_20_INS_TARGETED_THERAPY,
                DrugType.EGFR_TYROSINE_KINASE_INHIBITOR
            ),
            "MET" to setOf(
                DrugType.MET_INHIBITOR,
                DrugType.MET_TYROSINE_KINASE_INHIBITOR
            ),
            "RET" to setOf(
                DrugType.RET_INHIBITOR,
                DrugType.RET_TYROSINE_KINASE_INHIBITOR
            ),
            "ROS1" to setOf(
                DrugType.ROS1_INHIBITOR,
                DrugType.ROS1_TYROSINE_KINASE_INHIBITOR
            ),
            "BRAF" to setOf(
                DrugType.BRAF_INHIBITOR,
                DrugType.BRAF_TYROSINE_KINASE_INHIBITOR
            ),
            "NTRK1" to setOf(
                DrugType.TRK_RECEPTOR_INHIBITOR,
                DrugType.TRK_TYROSINE_KINASE_INHIBITOR
            ),
            "NTRK2" to setOf(
                DrugType.TRK_RECEPTOR_INHIBITOR,
                DrugType.TRK_TYROSINE_KINASE_INHIBITOR
            ),
            "NTRK3" to setOf(
                DrugType.TRK_RECEPTOR_INHIBITOR,
                DrugType.TRK_TYROSINE_KINASE_INHIBITOR
            )
        )
    }
}