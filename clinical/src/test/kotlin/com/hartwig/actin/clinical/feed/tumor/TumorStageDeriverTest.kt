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
    private val liverCancerWithNoStage = TumorDetails(doids = setOf(DoidConstants.LIVER_CANCER_DOID))

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
    fun `Should return stage III or IV when non-categorized cancer and one categorized location`() {
        assertThat(tumorStageDeriver.derive(breastCancerWithNoStage.copy(hasLymphNodeLesions = true))).containsOnly(
            TumorStage.III,
            TumorStage.IV
        )
        assertThat(tumorStageDeriver.derive(breastCancerWithNoStage.copy(hasLiverLesions = true))).containsOnly(
            TumorStage.III,
            TumorStage.IV
        )
    }

    @Test
    fun `Should return stage III or IV when non-categorized cancer and one uncategorized location`() {
        assertThat(tumorStageDeriver.derive(breastCancerWithNoStage.copy(otherLesions = listOf("lesion"))))
            .containsOnly(TumorStage.III, TumorStage.IV)
    }

    @Test
    fun `Should return null when categorized cancer with only associated lesions`() {
        assertThat(tumorStageDeriver.derive(lungCancerWithNoStage.copy(hasLungLesions = true))).isNull()
        assertThat(tumorStageDeriver.derive(liverCancerWithNoStage.copy(hasLiverLesions = true))).isNull()
    }

    @Test
    fun `Should return stage III or IV when categorized cancer and one uncategorized location`() {
        assertThat(tumorStageDeriver.derive(lungCancerWithNoStage.copy(otherLesions = listOf("lesion"))))
            .containsOnly(TumorStage.III, TumorStage.IV)
    }

    @Test
    fun `Should return stage IV when non-categorized cancer and at least one other location`() {
        assertThat(tumorStageDeriver.derive(breastCancerWithNoStage.copy(hasBoneLesions = true, hasBrainLesions = true))).containsOnly(
            TumorStage.IV
        )
        assertThat(
            tumorStageDeriver.derive(
                breastCancerWithNoStage.copy(
                    hasBoneLesions = true,
                    hasBrainLesions = true,
                    hasLymphNodeLesions = true
                )
            )
        ).containsOnly(TumorStage.IV)
    }

    @Test
    fun `Should return stage III or IV when categorized cancer and associated lesions and one other location`() {
        assertThat(tumorStageDeriver.derive(lungCancerWithNoStage.copy(hasLungLesions = true, hasLymphNodeLesions = true)))
            .containsOnly(TumorStage.III, TumorStage.IV)
        assertThat(tumorStageDeriver.derive(liverCancerWithNoStage.copy(hasLiverLesions = true, hasLymphNodeLesions = true)))
            .containsOnly(TumorStage.III, TumorStage.IV)
    }

    @Test
    fun `Should return stage IV when categorized cancer and at least two other locations`() {
        assertThat(
            tumorStageDeriver.derive(
                lungCancerWithNoStage.copy(
                    hasLungLesions = true,
                    hasBoneLesions = true,
                    hasLymphNodeLesions = true
                )
            )
        ).containsOnly(TumorStage.IV)
        assertThat(
            tumorStageDeriver.derive(
                liverCancerWithNoStage.copy(
                    hasLiverLesions = true,
                    hasBoneLesions = true,
                    hasLymphNodeLesions = true
                )
            )
        ).containsOnly(TumorStage.IV)
    }

    @Test
    fun `Should also count suspected lesions as lesions in stage derivation`() {
        assertThat(
            tumorStageDeriver.derive(
                breastCancerWithNoStage.copy(
                    hasSuspectedLungLesions = true
                )
            )
        ).containsOnly(TumorStage.III, TumorStage.IV)

        assertThat(
            tumorStageDeriver.derive(
                breastCancerWithNoStage.copy(
                    hasLungLesions = true,
                    hasSuspectedBoneLesions = true
                )
            )
        ).containsOnly(TumorStage.IV)
    }
}