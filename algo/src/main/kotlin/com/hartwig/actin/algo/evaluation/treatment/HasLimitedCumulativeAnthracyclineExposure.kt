package com.hartwig.actin.algo.evaluation.treatment

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.doid.DoidConstants
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.util.ValueComparison.stringCaseInsensitivelyMatchesQueryCollection
import com.hartwig.actin.clinical.datamodel.treatment.TreatmentCategory
import com.hartwig.actin.doid.DoidModel

class HasLimitedCumulativeAnthracyclineExposure(private val doidModel: DoidModel) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        var hasSuspectPriorTumorWithSuspectTreatmentHistory = false
        for (priorSecondPrimary in record.clinical().priorSecondPrimaries()) {
            if (hasSuspiciousCancerType(priorSecondPrimary.doids())
                && hasSuspiciousTreatmentHistory(priorSecondPrimary.treatmentHistory())
            ) {
                hasSuspectPriorTumorWithSuspectTreatmentHistory = true
            }
        }
        val hasSuspectPrimaryTumor = hasSuspiciousCancerType(record.clinical().tumor().doids())
        var hasAnthracyclineChemo = false
        var hasChemoWithoutType = false
        for (priorTumorTreatment in record.clinical().priorTumorTreatments()) {
            if (TreatmentTypeResolver.isOfType(priorTumorTreatment, TreatmentCategory.CHEMOTHERAPY, ANTHRACYCLINE_CHEMO_TYPE)) {
                hasAnthracyclineChemo = true
            } else if (priorTumorTreatment.categories().contains(TreatmentCategory.CHEMOTHERAPY)
                && !TreatmentTypeResolver.hasTypeConfigured(priorTumorTreatment, TreatmentCategory.CHEMOTHERAPY)
            ) {
                hasChemoWithoutType = true
            }
        }
        return when {
            hasAnthracyclineChemo -> {
                EvaluationFactory.undetermined(
                    "Patient has received anthracycline chemotherapy, exact dosage cannot be determined",
                    "Undetermined dosage of anthracycline exposure"
                )
            }

            hasChemoWithoutType && hasSuspectPrimaryTumor -> {
                EvaluationFactory.undetermined(
                    "Patient has cancer type that is associated with potential anthracycline chemotherapy, "
                            + "undetermined if anthracycline chemotherapy has been given", "Undetermined (dosage of) anthracycline exposure"
                )
            }

            hasSuspectPriorTumorWithSuspectTreatmentHistory -> {
                EvaluationFactory.undetermined(
                    "Patient has had a prior tumor that is associated with potential anthracycline chemotherapy",
                    "Undetermined (dosage of) anthracycline exposure"
                )
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
        if (tumorDoids == null) {
            return false
        }
        val expandedDoids = tumorDoids.flatMap { doidModel.doidWithParents(it) }.toSet()
        return CANCER_DOIDS_FOR_ANTHRACYCLINE.any { expandedDoids.contains(it) }
    }

    companion object {
        const val ANTHRACYCLINE_CHEMO_TYPE = "Anthracycline"
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