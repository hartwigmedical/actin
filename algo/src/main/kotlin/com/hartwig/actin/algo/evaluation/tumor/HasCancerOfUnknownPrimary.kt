package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.doid.DoidConstants
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.doid.DoidModel
import com.hartwig.actin.trial.input.datamodel.TumorTypeInput

class HasCancerOfUnknownPrimary(private val doidModel: DoidModel, private val categoryOfCUP: TumorTypeInput) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val tumorDoids = record.tumor.doids
        if (!DoidEvaluationFunctions.hasConfiguredDoids(tumorDoids)) {
            return EvaluationFactory.undetermined("Undetermined if patient has CUP (unknown tumor location/type)")
        }
        val tumorSubLocation = record.tumor.primaryTumorSubLocation
        val isCUP = tumorSubLocation != null && tumorSubLocation == CUP_PRIMARY_TUMOR_SUB_LOCATION
        val hasCorrectCUPCategory = DoidEvaluationFunctions.isOfExclusiveDoidType(
            doidModel, tumorDoids, categoryOfCUP.doid()
        )
        val hasOrganSystemCancer = DoidEvaluationFunctions.isOfDoidType(doidModel, tumorDoids, DoidConstants.ORGAN_SYSTEM_CANCER_DOID)
        if (hasCorrectCUPCategory && !hasOrganSystemCancer) {
            return if (isCUP) {
                EvaluationFactory.pass("Has CUP")
            } else {
                EvaluationFactory.warn("Uncertain if tumor type " + categoryOfCUP.display() + "is actually CUP")
            }
        }
        return if (DoidEvaluationFunctions.isOfExactDoid(tumorDoids, DoidConstants.CANCER_DOID)) {
            if (isCUP) {
                EvaluationFactory.undetermined("Cancer type is CUP but exact tumor type is unknown")
            } else {
                EvaluationFactory.undetermined("Undetermined if unknown tumor type can be considered CUP")
            }
        } else EvaluationFactory.fail("Has no CUP")
    }

    companion object {
        const val CUP_PRIMARY_TUMOR_SUB_LOCATION: String = "CUP"
    }
}