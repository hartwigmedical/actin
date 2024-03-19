package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.doid.DoidConstants
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.doid.DoidModel
import com.hartwig.actin.trial.input.datamodel.TumorTypeInput

class HasCancerOfUnknownPrimary (private val doidModel: DoidModel, private val categoryOfCUP: TumorTypeInput) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val tumorDoids = record.tumor.doids
        if (!DoidEvaluationFunctions.hasConfiguredDoids(tumorDoids)) {
            return EvaluationFactory.undetermined(
                "No tumor location/type configured for patient, cancer of unknown primary (CUP) status undetermined",
                "Unconfigured tumor location/type"
            )
        }
        val tumorSubLocation = record.tumor.primaryTumorSubLocation
        val isCUP = tumorSubLocation != null && tumorSubLocation == CUP_PRIMARY_TUMOR_SUB_LOCATION
        val hasCorrectCUPCategory = DoidEvaluationFunctions.isOfExclusiveDoidType(
            doidModel, tumorDoids, categoryOfCUP.doid()
        )
        val hasOrganSystemCancer = DoidEvaluationFunctions.isOfDoidType(doidModel, tumorDoids, DoidConstants.ORGAN_SYSTEM_CANCER_DOID)
        if (hasCorrectCUPCategory && !hasOrganSystemCancer) {
            return if (isCUP) {
                EvaluationFactory.pass("Patient has cancer of unknown primary (CUP) of type " + categoryOfCUP.display(), "Tumor type")
            } else {
                EvaluationFactory.warn(
                    "Patient has cancer of type " + categoryOfCUP.display() +
                            ", but not explicitly configured as cancer of unknown primary (CUP), hence may not actually be a CUP?",
                    "Tumor type " + categoryOfCUP.display() + " - uncertain if actually CUP"
                )
            }
        }
        return if (DoidEvaluationFunctions.isOfExactDoid(tumorDoids, DoidConstants.CANCER_DOID)) {
            if (isCUP) {
                EvaluationFactory.undetermined(
                    "Cancer type is cancer of unknown primary (CUP), but exact tumor type is unknown",
                    "Undetermined CUP tumor type"
                )
            } else {
                EvaluationFactory.undetermined(
                    "Tumor type is unknown, and cancer is not explicitly configured as cancer of unknown primary (CUP) - hence undetermined if actually CUP?",
                    "Undetermined if CUP tumor type"
                )
            }
        } else EvaluationFactory.fail("Patient has no cancer of unknown primary (CUP) of type " + categoryOfCUP.display(), "Tumor type")
    }

    companion object {
        const val CUP_PRIMARY_TUMOR_SUB_LOCATION: String = "CUP"
    }
}