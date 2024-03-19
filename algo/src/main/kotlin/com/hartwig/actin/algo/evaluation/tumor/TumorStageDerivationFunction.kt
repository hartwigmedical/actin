package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.doid.DoidConstants
import com.hartwig.actin.clinical.datamodel.TumorDetails
import com.hartwig.actin.clinical.datamodel.TumorStage
import com.hartwig.actin.doid.DoidModel
import java.util.function.Predicate

internal class TumorStageDerivationFunction private constructor(private val derivationRules: Map<Predicate<TumorDetails>, Set<TumorStage>>) {
    fun apply(tumor: TumorDetails): List<TumorStage> {
        return if (DoidEvaluationFunctions.hasConfiguredDoids(tumor.doids) && hasNoTumorStage(tumor)) {
            derivationRules.entries
                .filter { it.key.test(tumor) }
                .flatMap { it.value }
        } else {
            emptyList()
        }
    }

    companion object {
        fun create(doidModel: DoidModel): TumorStageDerivationFunction {
            return TumorStageDerivationFunction(
                mapOf(
                    hasAllKnownLesionDetails().and(
                        hasExactlyCategorizedLesions(0, doidModel).and(
                            hasNoUncategorizedLesions()
                        )
                    ) to setOf(TumorStage.I, TumorStage.II),
                    hasExactlyCategorizedLesions(1, doidModel) to setOf(TumorStage.III, TumorStage.IV),
                    hasAtLeastCategorizedLesions(2, doidModel) to setOf(TumorStage.IV)
                )
            )
        }

        private fun hasNoTumorStage(tumor: TumorDetails): Boolean {
            return tumor.stage == null
        }

        private fun hasAtLeastCategorizedLesions(min: Int, doidModel: DoidModel): Predicate<TumorDetails> {
            return Predicate<TumorDetails> { tumor: TumorDetails -> lesionCount(doidModel, tumor) >= min }
        }

        private fun hasExactlyCategorizedLesions(count: Int, doidModel: DoidModel): Predicate<TumorDetails> {
            return Predicate<TumorDetails> { tumor: TumorDetails -> lesionCount(doidModel, tumor) == count }
        }

        private fun lesionCount(doidModel: DoidModel, tumor: TumorDetails): Int {
            return listOf(
                Triple(tumor.hasLiverLesions, tumor.liverLesionsCount, DoidConstants.LIVER_CANCER_DOID),
                Triple(tumor.hasLymphNodeLesions, tumor.lymphNodeLesionsCount, DoidConstants.LYMPH_NODE_CANCER_DOID),
                Triple(tumor.hasCnsLesions, tumor.cnsLesionsCount, DoidConstants.CNS_CANCER_DOID),
                Triple(tumor.hasBrainLesions, tumor.brainLesionsCount, DoidConstants.BRAIN_CANCER_DOID),
                Triple(tumor.hasLungLesions, tumor.lungLesionsCount,  DoidConstants.LUNG_CANCER_DOID),
                Triple(tumor.hasBoneLesions, tumor.boneLesionsCount, DoidConstants.BONE_CANCER_DOID)
            ).count {
                evaluateMetastases(it.first, it.second, tumor.doids, it.third, doidModel)
            }
        }

        private fun hasNoUncategorizedLesions(): Predicate<TumorDetails> {
            return Predicate<TumorDetails> { tumor: TumorDetails -> tumor.otherLesions.isNullOrEmpty() }
        }

        private fun hasAllKnownLesionDetails(): Predicate<TumorDetails> {
            return Predicate<TumorDetails> { tumor: TumorDetails ->
                listOf(
                    tumor.hasLiverLesions,
                    tumor.hasLymphNodeLesions,
                    tumor.hasCnsLesions,
                    tumor.hasBrainLesions,
                    tumor.hasLungLesions,
                    tumor.hasBoneLesions
                ).all { it != null }
            }
        }

        private fun evaluateMetastases(hasLesions: Boolean?, lesionsCount: Int?, tumorDoids: Set<String>?, doidToMatch: String, doidModel: DoidModel): Boolean {
            return if (!DoidEvaluationFunctions.isOfDoidType(doidModel, tumorDoids, DoidConstants.LUNG_CANCER_DOID) || doidToMatch != DoidConstants.LUNG_CANCER_DOID) {
                (hasLesions ?: false) && !DoidEvaluationFunctions.isOfDoidType(doidModel, tumorDoids, doidToMatch)
            }
            else (lesionsCount ?: 0) >= 2
        }
    }
}