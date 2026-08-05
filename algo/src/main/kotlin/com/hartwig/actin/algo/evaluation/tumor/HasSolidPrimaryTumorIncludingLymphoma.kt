package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.doid.DoidConstants
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.algo.evaluation.EvaluationLabels
import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.doid.DoidModel

class HasSolidPrimaryTumorIncludingLymphoma(private val doidModel: DoidModel, private val labels: EvaluationLabels.Tumor) :
    EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val tumorDoids = record.tumor.doids
        if (!DoidEvaluationFunctions.hasConfiguredDoids(tumorDoids)) {
            return EvaluationFactory.undetermined(labels.hasSolidPrimaryTumorIncludingLymphomaUndetermined())
        }
        val result = DoidEvaluationFunctions.evaluateAllDoidsMatchWithFailAndWarns(
            doidModel,
            tumorDoids,
            setOf(DoidConstants.CANCER_DOID, DoidConstants.BENIGN_NEOPLASM_DOID),
            DoidConstants.NON_SOLID_CANCER_DOIDS,
            WARN_SOLID_CANCER_DOIDS
        )
        return when (result) {
            EvaluationResult.FAIL -> {
                EvaluationFactory.fail(labels.hasSolidPrimaryTumorIncludingLymphomaFail())
            }

            EvaluationResult.WARN -> {
                EvaluationFactory.warn(labels.hasSolidPrimaryTumorIncludingLymphomaWarn())
            }

            EvaluationResult.PASS -> {
                EvaluationFactory.pass(labels.hasSolidPrimaryTumorIncludingLymphomaPass())
            }

            else -> {
                Evaluation(result = result, recoverable = false)
            }
        }
    }

    companion object {
        val WARN_SOLID_CANCER_DOIDS = setOf(
            DoidConstants.CENTRAL_NERVOUS_SYSTEM_HEMATOLOGIC_CANCER_DOID,
            DoidConstants.DENDRITIC_CELL_THYMOMA_DOID,
            DoidConstants.HISTIOCYTIC_AND_DENDRITIC_CELL_CANCER_DOID,
            DoidConstants.MAST_CELL_NEOPLASM_DOID,
            DoidConstants.MYELOID_SARCOMA_DOID,
        )
    }
}