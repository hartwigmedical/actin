package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.doid.DoidConstants
import com.hartwig.actin.algo.evaluation.tumor.DoidEvaluationFunctions.isOfAtLeastOneDoidType
import com.hartwig.actin.algo.evaluation.tumor.DoidEvaluationFunctions.isOfAtLeastOneDoidTerm
import com.hartwig.actin.datamodel.clinical.TumorDetails
import com.hartwig.actin.datamodel.clinical.TumorStage
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

    fun hasTumorWithLargeCellComponent(doidModel: DoidModel, tumorDoids: Set<String>?, tumorName: String): Boolean {
        val hasLargeCellDoid = isOfAtLeastOneDoidType(doidModel, tumorDoids, DoidConstants.LARGE_CELL_CANCER_DOIDS)
        val hasLargeCellName = TumorTermConstants.LARGE_CELL_TERMS.any {
            tumorName.lowercase().contains(it)
        } && !TumorTermConstants.NON_SMALL_CELL_TERMS.any { tumorName.lowercase().contains(it) }
        return hasLargeCellDoid || hasLargeCellName
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

    fun isStageMatch(stage: TumorStage, stagesToMatch: Set<TumorStage>): Boolean {
        if (stagesToMatch.any { it.category != null }) throw IllegalArgumentException("This function cannot be used to evaluate specific (non-categorical) stages. Use isSpecificStageMatch instead.")
        return stage in stagesToMatch || stage.category in stagesToMatch
    }

    fun isSpecificStageMatch(stage: TumorStage, stagesToMatch: Set<TumorStage>): Pair<Boolean, Boolean> {
        val isCertainMatch = stage in stagesToMatch || stage.category in stagesToMatch
        val isPotentialMatch = stagesToMatch.any { it.category == stage }
        return Pair(isCertainMatch, isPotentialMatch)
    }

    private fun evaluatePeritonealMetastases(lesions: List<String>?): Boolean? {
        val targetTerms = listOf("peritoneum", "peritoneal", "intraperitoneum", "intraperitoneal")
        return lesions?.any { lesion ->
            val lowercaseLesion = lesion.lowercase()
            targetTerms.any(lowercaseLesion::startsWith) || targetTerms.any { lowercaseLesion.contains(" $it") }
        }
    }
}