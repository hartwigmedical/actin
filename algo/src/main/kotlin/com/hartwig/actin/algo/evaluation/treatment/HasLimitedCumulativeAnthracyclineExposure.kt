package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.algo.doid.DoidConstants
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.util.ValueComparison.stringCaseInsensitivelyMatchesQueryCollection
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.clinical.treatment.DrugType
import com.hartwig.actin.datamodel.clinical.treatment.TreatmentCategory
import com.hartwig.actin.doid.DoidModel

class HasLimitedCumulativeAnthracyclineExposure(private val doidModel: DoidModel) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val hasSuspectPriorTumorWithSuspectTreatmentHistory = record.priorSecondPrimaries.any {
            hasSuspiciousCancerType(it.doids) && hasSuspiciousTreatmentHistory(it.treatmentHistory)
        }
        val hasSuspectPrimaryTumor = hasSuspiciousCancerType(record.tumor.doids)

        val anthracyclineSummary = TreatmentSummaryForCategory.createForTreatmentHistory(
            record.oncologicalHistory, TreatmentCategory.CHEMOTHERAPY, { historyEntry -> historyEntry.isOfType(DrugType.ANTHRACYCLINE) }
        )

        return when {
            anthracyclineSummary.hasSpecificMatch() -> {
                EvaluationFactory.undetermined(
                    "Patient has received anthracycline chemotherapy, exact dosage cannot be determined",
                    "Undetermined dosage of anthracycline exposure"
                )
            }

            anthracyclineSummary.hasApproximateMatch() && hasSuspectPrimaryTumor -> {
                EvaluationFactory.undetermined(
                    "Patient has cancer type that is associated with potential anthracycline chemotherapy, "
                            + "undetermined if anthracycline chemotherapy has been given", "Undetermined (dosage of) anthracycline exposure"
                )
            }

            hasSuspectPriorTumorWithSuspectTreatmentHistory -> {
                val message = "Undetermined if prior anthracycline exposure within permitted limit " +
                        "(prior tumor in history associated with anthracycline chemotherapy)"
                EvaluationFactory.undetermined(message, message)
            }

            else -> {
                EvaluationFactory.pass(
                    "Patient should not have been exposed to anthracycline chemotherapy, thus not exceeding maximum dose",
                    "Limited cumulative anthracycline exposure"
                )
            }
        }
    }

    private fun hasSuspiciousCancerType(tumorDoids: Set<String>?): Boolean {
        return tumorDoids != null && tumorDoids.flatMap { doidModel.doidWithParents(it) }
            .any { CANCER_DOIDS_FOR_ANTHRACYCLINE.contains(it) }
    }

    companion object {
        val CANCER_DOIDS_FOR_ANTHRACYCLINE: Set<String> = setOf(
            DoidConstants.BREAST_CANCER_DOID,
            DoidConstants.LYMPH_NODE_CANCER_DOID,
            DoidConstants.SARCOMA_DOID,
            DoidConstants.OVARIAN_CANCER_DOID,
            DoidConstants.MULTIPLE_MYELOMA_DOID,
            DoidConstants.LEUKEMIA_DOID
        )
        val PRIOR_PRIMARY_SUSPICIOUS_TREATMENTS: Set<String> = setOf("chemotherapy", "anthracycline")

        private fun hasSuspiciousTreatmentHistory(priorPrimaryTreatmentHistory: String): Boolean {
            return priorPrimaryTreatmentHistory.isEmpty() ||
                    stringCaseInsensitivelyMatchesQueryCollection(priorPrimaryTreatmentHistory, PRIOR_PRIMARY_SUSPICIOUS_TREATMENTS)
        }
    }
}