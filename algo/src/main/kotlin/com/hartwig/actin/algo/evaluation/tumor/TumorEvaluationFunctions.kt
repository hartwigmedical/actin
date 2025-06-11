package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.doid.DoidConstants
import com.hartwig.actin.algo.evaluation.tumor.DoidEvaluationFunctions.isOfAtLeastOneDoidType
import com.hartwig.actin.algo.evaluation.tumor.DoidEvaluationFunctions.isOfAtLeastOneDoidTerm
import com.hartwig.actin.datamodel.clinical.TumorDetails
import com.hartwig.actin.doid.DoidModel

object TumorEvaluationFunctions {

    fun hasTumorWithNeuroendocrineComponent(doidModel: DoidModel, tumorDoids: Set<String>?, tumorName: String): Boolean {
        val hasNeuroendocrineDoid = isOfAtLeastOneDoidType(doidModel, tumorDoids, DoidConstants.NEUROENDOCRINE_DOIDS)
        val hasNeuroendocrineDoidTerm = isOfAtLeastOneDoidTerm(doidModel, tumorDoids, TumorTermConstants.NEUROENDOCRINE_TERMS)
        val hasNeuroendocrineName = TumorTermConstants.NEUROENDOCRINE_TERMS.any { tumorName.lowercase().contains(it) }
        return hasNeuroendocrineDoid || hasNeuroendocrineDoidTerm || hasNeuroendocrineName
    }

    fun hasTumorWithSmallCellComponent(doidModel: DoidModel, tumorDoids: Set<String>?, tumorName: String): Boolean {
        val hasSmallCellDoid = isOfAtLeastOneDoidType(doidModel, tumorDoids, DoidConstants.SMALL_CELL_CANCER_DOIDS)
        val hasSmallCellName = TumorTermConstants.SMALL_CELL_TERMS.any {
            tumorName.lowercase().contains(it)
        } && !TumorTermConstants.NON_SMALL_CELL_TERMS.any { tumorName.lowercase().contains(it) }
        return hasSmallCellDoid || hasSmallCellName
    }

    fun hasCancerOfUnknownPrimary(tumorName: String): Boolean {
        return tumorName.contains(TumorTermConstants.CUP_TERM)
    }

    fun hasPeritonealMetastases(tumor: TumorDetails): Boolean? {
        return evaluatePeritonealMetastases(tumor.otherLesions)
    }

    fun hasSuspectedPeritonealMetastases(tumor: TumorDetails): Boolean? {
        return evaluatePeritonealMetastases(tumor.otherSuspectedLesions)
    }

    private fun evaluatePeritonealMetastases(lesions: List<String>?): Boolean? {
        val targetTerms = listOf("peritoneum", "peritoneal", "intraperitoneum", "intraperitoneal")
        return lesions?.any { lesion ->
            val lowercaseLesion = lesion.lowercase()
            targetTerms.any(lowercaseLesion::startsWith) || targetTerms.any { lowercaseLesion.contains(" $it") }
        }
    }
}