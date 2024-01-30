package com.hartwig.actin.algo.evaluation.tumor

import com.google.common.collect.Sets
import com.hartwig.actin.PatientRecord
import com.hartwig.actin.algo.datamodel.Evaluation
import com.hartwig.actin.algo.doid.DoidConstants
import com.hartwig.actin.algo.evaluation.EvaluationFactory
import com.hartwig.actin.algo.evaluation.EvaluationFunction
import com.hartwig.actin.doid.DoidModel

class HasProstateCancerWithSmallCellComponent (private val doidModel: DoidModel) : EvaluationFunction {

    override fun evaluate(record: PatientRecord): Evaluation {
        val tumorDoids = record.clinical.tumor.doids
        if (!DoidEvaluationFunctions.hasConfiguredDoids(tumorDoids)) {
            return EvaluationFactory.undetermined(
                "Could not determine whether patient has prostate cancer with small cell histology",
                "Undetermined prostate cancer with small cell histology"
            )
        }
        val isProstateSmallCellCarcinoma =
            DoidEvaluationFunctions.isOfDoidType(doidModel, tumorDoids, DoidConstants.PROSTATE_SMALL_CELL_CARCINOMA_DOID)
        val hasProstateCancer = DoidEvaluationFunctions.isOfDoidType(doidModel, tumorDoids, DoidConstants.PROSTATE_CANCER_DOID)
        val hasSmallCellDetails = TumorTypeEvaluationFunctions.hasTumorWithDetails(
            record.clinical.tumor,
            Sets.newHashSet(SMALL_CELL_DETAILS)
        )
        if (isProstateSmallCellCarcinoma || hasProstateCancer && hasSmallCellDetails) {
            return EvaluationFactory.pass("Patient has prostate cancer with small cell histology", "Tumor type")
        }
        val hasProstateWarnType = PROSTATE_WARN_DOID_SETS.any { warnDoidCombination ->
            warnDoidCombination.all { DoidEvaluationFunctions.isOfDoidType(doidModel, tumorDoids, it) }
        }
        if (hasProstateWarnType) {
            return EvaluationFactory.warn(
                "Patient has prostate cancer but potentially no small cell histology",
                "Undetermined prostate cancer with small cell histology"
            )
        }
        val isExactProstateCancer = DoidEvaluationFunctions.isOfExactDoid(tumorDoids, DoidConstants.PROSTATE_CANCER_DOID)
        return if (isExactProstateCancer) {
            EvaluationFactory.undetermined(
                "Patient has prostate cancer but with no configured histology subtype",
                "Undetermined prostate cancer with small cell histology"
            )
        } else {
            EvaluationFactory.fail("Patient has no prostate cancer with small cell histology", "Tumor type")
        }
    }

    companion object {
        const val SMALL_CELL_DETAILS: String = "small cell"
        val PROSTATE_WARN_DOID_SETS = setOf(
            setOf(DoidConstants.PROSTATE_NEUROENDOCRINE_NEOPLASM),
            setOf(DoidConstants.PROSTATE_CANCER_DOID, DoidConstants.NEUROENDOCRINE_CARCINOMA_DOID),
            setOf(DoidConstants.PROSTATE_CANCER_DOID, DoidConstants.NEUROENDOCRINE_TUMOR_DOID)
        )
    }
}