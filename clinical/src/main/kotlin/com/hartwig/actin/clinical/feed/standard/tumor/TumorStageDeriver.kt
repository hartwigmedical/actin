package com.hartwig.actin.clinical.feed.standard.tumor

import com.hartwig.actin.algo.doid.DoidConstants
import com.hartwig.actin.algo.evaluation.tumor.DoidEvaluationFunctions
import com.hartwig.actin.clinical.datamodel.TumorDetails
import com.hartwig.actin.clinical.datamodel.TumorStage
import com.hartwig.actin.doid.DoidModel
import java.util.function.Predicate

class TumorStageDeriver private constructor(private val derivationRules: Map<Predicate<TumorDetails>, Set<TumorStage>>) {
    fun derive(tumor: TumorDetails): Set<TumorStage>? {
        return if (DoidEvaluationFunctions.hasConfiguredDoids(tumor.doids) && hasNoTumorStage(tumor)) {
            derivationRules.entries.firstOrNull { it.key.test(tumor) }
                ?.value
        } else {
            null
        }
    }

    companion object {
        fun create(doidModel: DoidModel): TumorStageDeriver {
            return TumorStageDeriver(
                linkedMapOf(
                    hasAtLeastCategorizedLesions(2, doidModel) to setOf(TumorStage.IV),
                    hasExactlyCategorizedLesions(1, doidModel).or(hasUncategorizedLesions()) to setOf(TumorStage.III, TumorStage.IV),
                    hasAllKnownLesionDetails().and(
                        hasExactlyCategorizedLesions(0, doidModel).and(
                            hasNoUncategorizedLesions()
                        )
                    ) to setOf(TumorStage.I, TumorStage.II),
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
                tumor.hasLiverLesions to DoidConstants.LIVER_CANCER_DOID,
                tumor.hasLymphNodeLesions to DoidConstants.LYMPH_NODE_CANCER_DOID,
                tumor.hasCnsLesions to DoidConstants.CNS_CANCER_DOID,
                tumor.hasBrainLesions to DoidConstants.BRAIN_CANCER_DOID,
                tumor.hasLungLesions to DoidConstants.LUNG_CANCER_DOID,
                tumor.hasBoneLesions to DoidConstants.BONE_CANCER_DOID
            ).count { (hasLesions, doidToMatch) ->
                evaluateMetastases(hasLesions, tumor, doidToMatch, doidModel)
            }
        }

        private fun hasNoUncategorizedLesions(): Predicate<TumorDetails> {
            return Predicate<TumorDetails> { tumor: TumorDetails -> tumor.otherLesions.isNullOrEmpty() }
        }

        private fun hasUncategorizedLesions(): Predicate<TumorDetails> {
            return Predicate<TumorDetails> { tumor: TumorDetails -> tumor.otherLesions?.isNotEmpty() ?: false }
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

        private fun evaluateMetastases(hasLesions: Boolean?, tumor: TumorDetails, doidToMatch: String, doidModel: DoidModel): Boolean {
            // Currently only for lung cancer multiple lesions are resolved to stage III/IV
            return if (checkingLungMetastasesForLungCancer(doidModel, doidToMatch, tumor.doids) && tumor.lungLesionsCount != null) {
                tumor.lungLesionsCount!! >= 2
            } else {
                (hasLesions ?: false) && !DoidEvaluationFunctions.isOfDoidType(doidModel, tumor.doids, doidToMatch)
            }
        }

        private fun checkingLungMetastasesForLungCancer(doidModel: DoidModel, doidToMatch: String, tumorDoids: Set<String>?): Boolean {
            return DoidEvaluationFunctions.isOfDoidType(
                doidModel,
                tumorDoids,
                DoidConstants.LUNG_CANCER_DOID
            ) && doidToMatch == DoidConstants.LUNG_CANCER_DOID
        }
    }
}