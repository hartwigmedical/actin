package com.hartwig.actin.clinical.feed.tumor

import com.hartwig.actin.algo.doid.DoidConstants
import com.hartwig.actin.datamodel.clinical.TumorDetails
import com.hartwig.actin.datamodel.clinical.TumorStage
import com.hartwig.actin.doid.TestDoidModelFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class TumorStageDeriverTest {
    private val doidModel = TestDoidModelFactory.createMinimalTestDoidModel()
    private val tumorStageDeriver = TumorStageDeriver.create(doidModel)
    private val breastCancerWithNoStage = TumorDetails(doids = setOf(DoidConstants.BREAST_CANCER_DOID))
    private val lungCancerWithNoStage = TumorDetails(doids = setOf(DoidConstants.LUNG_CANCER_DOID))

    @Test
    fun `Should return null when no doids configured`() {
        assertThat(tumorStageDeriver.derive(TumorDetails(doids = null))).isNull()
    }

    @Test
    fun `Should return null when no lesion details configured`() {
        assertThat(tumorStageDeriver.derive(breastCancerWithNoStage)).isNull()
    }

    @Test
    fun `Should return stage I and II when no lesions`() {
        assertThat(
            tumorStageDeriver.derive(
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
        assertThat(tumorStageDeriver.derive(breastCancerWithNoStage.copy(hasLymphNodeLesions = true)))
            .containsOnly(TumorStage.III, TumorStage.IV)
    }

    @Test
    fun `Should return stage IV when multiple lesions`() {
        assertThat(tumorStageDeriver.derive(breastCancerWithNoStage.copy(hasBoneLesions = true, hasBrainLesions = true)))
            .containsOnly(TumorStage.IV)
        assertThat(tumorStageDeriver.derive(lungCancerWithNoStage.copy(hasLungLesions = true, lungLesionsMinCount = 3, hasBoneLesions = true)))
            .containsOnly(TumorStage.IV)
    }

    @Test
    fun `Should include suspected lesions into stage derivation`() {
        assertThat(
            tumorStageDeriver.derive(
                breastCancerWithNoStage.copy(
                    hasSuspectedLungLesions = true,
                    hasSuspectedBoneLesions = true,
                    hasSuspectedBrainLesions = true
                )
            )
        ).containsOnly(TumorStage.IV)

        assertThat(
            tumorStageDeriver.derive(
                breastCancerWithNoStage.copy(
                    hasLungLesions = true,
                    hasSuspectedBoneLesions = true,
                    otherLesions = listOf("lesion")
                )
            )
        ).containsOnly(TumorStage.IV)
    }

    @Test
    fun `Should return stage III and IV when one uncategorized location`() {
        assertThat(tumorStageDeriver.derive(breastCancerWithNoStage.copy(otherLesions = listOf("lesion"))))
            .containsOnly(TumorStage.III, TumorStage.IV)
    }

    @Test
    fun `Should return stage III and IV when lung cancer with other lung lesions besides the primary lung cancer`() {
        assertThat(tumorStageDeriver.derive(lungCancerWithNoStage.copy(hasLungLesions = true, lungLesionsMinCount = 3)))
            .containsOnly(TumorStage.III, TumorStage.IV)
    }
}