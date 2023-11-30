package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.doid.DoidConstants
import com.hartwig.actin.algo.evaluation.EvaluationFactory.unrecoverable
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.doid.DoidModel

class HasSolidPrimaryTumor (private val doidModel: DoidModel) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val tumorDoids = record.clinical().tumor().doids()
        if (!DoidEvaluationFunctions.hasConfiguredDoids(tumorDoids)) {
            return unrecoverable().result(EvaluationResult.UNDETERMINED)
                .addUndeterminedSpecificMessages("No tumor location/type configured for patient, unknown if solid primary tumor")
                .addUndeterminedGeneralMessages("Undetermined solid primary tumor").build()
        }
        val result = DoidEvaluationFunctions.evaluateAllDoidsMatchWithFailAndWarns(
            doidModel,
            tumorDoids,
            setOf(DoidConstants.CANCER_DOID, DoidConstants.BENIGN_NEOPLASM_DOID),
            NON_SOLID_CANCER_DOIDS,
            WARN_SOLID_CANCER_DOIDS
        )
        val builder = unrecoverable().result(result)
        when (result) {
            EvaluationResult.FAIL -> {
                builder.addFailSpecificMessages("Patient has non-solid primary tumor")
                builder.addFailGeneralMessages("Tumor type")
            }

            EvaluationResult.WARN -> {
                builder.addWarnSpecificMessages("Unclear if tumor type of patient should be considered solid or non-solid")
                builder.addWarnGeneralMessages("Unclear if primary tumor is considered solid")
            }

            EvaluationResult.PASS -> {
                builder.addPassSpecificMessages("Patient has solid primary tumor")
                builder.addPassGeneralMessages("Tumor type")
            }

            else -> {}
        }
        return builder.build()
    }

    companion object {
        val NON_SOLID_CANCER_DOIDS =
            setOf(DoidConstants.LEUKEMIA_DOID, DoidConstants.REFRACTORY_HEMATOLOGIC_CANCER_DOID, DoidConstants.BONE_MARROW_CANCER_DOID)
        val WARN_SOLID_CANCER_DOIDS = setOf(DoidConstants.HEMATOLOGIC_CANCER_DOID)
    }
}