package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.doid.DoidConstants
import com.hartwig.actin.clinical.datamodel.TumorDetails
import com.hartwig.actin.clinical.datamodel.TumorStage
import com.hartwig.actin.doid.TestDoidModelFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class TumorStageDerivationFunctionTest {
    private val derivationFunction = TumorStageDerivationFunction.create(TestDoidModelFactory.createMinimalTestDoidModel())
    private val tumorDetailsWithNoStage = TumorDetails(doids = setOf(DoidConstants.BREAST_CANCER_DOID))

    @Test
    fun `Should return null when no doids configured`() {
        assertThat(derivationFunction.apply(TumorDetails(doids = null))).isNull()
    }

    @Test
    fun `Should return empty set of derivation when no lesion details configured`() {
        assertThat(derivationFunction.apply(tumorDetailsWithNoStage)).isEmpty()
    }

    @Test
    fun `Should return stage I and II when no lesions`() {
        assertThat(
            derivationFunction.apply(
                tumorDetailsWithNoStage.copy(
                    hasBoneLesions = false,
                    hasBrainLesions = false,
                    hasLiverLesions = false,
                    hasLymphNodeLesions = false,
                    hasCnsLesions = false,
                    hasLungLesions = false
                )
            )
        ).containsOnly(TumorStage.I, TumorStage.II)
    }

    @Test
    fun `Should return stage III and IV when one categorized location`() {
        assertThat(derivationFunction.apply(tumorDetailsWithNoStage.copy(hasLymphNodeLesions = true)))
            .containsOnly(TumorStage.III, TumorStage.IV)
    }

    @Test
    fun `Should return stage III and IV when one uncategorized location`() {
        assertThat(derivationFunction.apply(tumorDetailsWithNoStage.copy(otherLesions = listOf("lesion"))))
            .containsOnly(TumorStage.III, TumorStage.IV)
    }

    @Test
    fun `Should return stage IV when multiple lesions`() {
        assertThat(derivationFunction.apply(tumorDetailsWithNoStage.copy(hasBoneLesions = true, hasBrainLesions = true)))
            .containsOnly(TumorStage.IV)
    }
}