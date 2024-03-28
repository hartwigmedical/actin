package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.doid.DoidConstants
import com.hartwig.actin.clinical.datamodel.TumorDetails
import com.hartwig.actin.clinical.datamodel.TumorStage
import com.hartwig.actin.doid.TestDoidModelFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class TumorStageDerivationFunctionTest {
    private val derivationFunction = TumorStageDerivationFunction.create(TestDoidModelFactory.createMinimalTestDoidModel())
    private val breastCancerWithNoStage = TumorDetails(doids = setOf(DoidConstants.BREAST_CANCER_DOID))
    private val lungCancerWithNoStage = TumorDetails(doids = setOf(DoidConstants.LUNG_CANCER_DOID))

    @Test
    fun `Should return null when no doids configured`() {
        assertThat(derivationFunction.apply(TumorDetails(doids = null))).isNull()
    }

    @Test
    fun `Should return null when no lesion details configured`() {
        assertThat(derivationFunction.apply(breastCancerWithNoStage)).isNull()
    }

    @Test
    fun `Should return stage I and II when no lesions`() {
        assertThat(
            derivationFunction.apply(
                breastCancerWithNoStage.copy(
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
        assertThat(derivationFunction.apply(breastCancerWithNoStage.copy(hasLymphNodeLesions = true)))
            .containsOnly(TumorStage.III, TumorStage.IV)
    }

    @Test
    fun `Should return stage IV when multiple lesions`() {
        assertThat(derivationFunction.apply(breastCancerWithNoStage.copy(hasBoneLesions = true, hasBrainLesions = true)))
            .containsOnly(TumorStage.IV)
        assertThat(derivationFunction.apply(lungCancerWithNoStage.copy(hasLungLesions = true, lungLesionsCount = 3, hasBoneLesions = true)))
            .containsOnly(TumorStage.IV)
    }

    @Test
    fun `Should return stage III and IV when one uncategorized location`() {
        assertThat(derivationFunction.apply(breastCancerWithNoStage.copy(otherLesions = listOf("lesion"))))
            .containsOnly(TumorStage.III, TumorStage.IV)
    }

    @Test
    fun `Should return stage III and IV when lung cancer with other lung lesions besides the primary lung cancer`() {
        assertThat(derivationFunction.apply(lungCancerWithNoStage.copy(hasLungLesions = true, lungLesionsCount = 3)))
            .containsOnly(TumorStage.III, TumorStage.IV)
    }
}