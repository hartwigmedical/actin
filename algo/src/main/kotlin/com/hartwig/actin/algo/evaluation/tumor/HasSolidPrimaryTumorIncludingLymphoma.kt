package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.datamodel.EvaluationResult
import com.hartwig.actin.algo.doid.DoidConstants
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.doid.DoidModel

class HasSolidPrimaryTumorIncludingLymphoma(private val doidModel: DoidModel) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val tumorDoids = record.tumor.doids
        if (!DoidEvaluationFunctions.hasConfiguredDoids(tumorDoids)) {
            return EvaluationFactory.undetermined(
                "No tumor location/type configured for patient, unknown if solid primary tumor or lymphoma",
                "Undetermined solid primary tumor"
            )
        }
        val result = DoidEvaluationFunctions.evaluateAllDoidsMatchWithFailAndWarns(
            doidModel,
            tumorDoids,
            setOf(DoidConstants.CANCER_DOID, DoidConstants.BENIGN_NEOPLASM_DOID),
            NON_SOLID_CANCER_DOIDS,
            WARN_SOLID_CANCER_DOIDS
        )
        return when (result) {
            EvaluationResult.FAIL -> {
                EvaluationFactory.fail("Patient has non-solid primary tumor", "Tumor type")
            }

            EvaluationResult.WARN -> {
                EvaluationFactory.warn(
                    "Unclear if tumor type of patient should be considered solid or non-solid",
                    "Unclear if primary tumor is considered solid"
                )
            }

            EvaluationResult.PASS -> {
                EvaluationFactory.pass("Patient has solid primary tumor (including lymphoma)", "Tumor type")
            }

            else -> {
                Evaluation(result = result, recoverable = false)
            }
        }
    }

    companion object {
        val NON_SOLID_CANCER_DOIDS =
            setOf(DoidConstants.LEUKEMIA_DOID, DoidConstants.REFRACTORY_HEMATOLOGIC_CANCER_DOID, DoidConstants.BONE_MARROW_CANCER_DOID)
        val WARN_SOLID_CANCER_DOIDS = setOf(
            DoidConstants.CENTRAL_NERVOUS_SYSTEM_HEMATOLOGIC_CANCER_DOID,
            DoidConstants.DENDRITIC_CELL_THYMOMA_DOID,
            DoidConstants.HISTIOCYTIC_AND_DENDRITIC_CELL_CANCER_DOID,
            DoidConstants.MAST_CELL_NEOPLASM_DOID,
            DoidConstants.MYELOID_SARCOMA_DOID,
        )
    }
}