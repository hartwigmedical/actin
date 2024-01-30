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
    fun shouldReturnEmptySetOfDerivationWhenNoDoidsConfigured() {
        assertThat(derivationFunction.apply(tumorDetailsWithNoStage)).isEmpty()
    }

    @Test
    fun shouldReturnEmptySetOfDerivationWhenNoLesionDetailsConfigured() {
        assertThat(derivationFunction.apply(tumorDetailsWithNoStage)).isEmpty()
    }

    @Test
    fun shouldReturnStageIAndIIWhenNoLesions() {
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
    fun shouldReturnStageIIIAndIVWhenOneCategorizedLocation() {
        assertThat(derivationFunction.apply(tumorDetailsWithNoStage.copy(hasLymphNodeLesions = true)))
            .containsOnly(TumorStage.III, TumorStage.IV)
    }

    @Test
    fun shouldReturnStageIVWhenMultipleLesions() {
        assertThat(derivationFunction.apply(tumorDetailsWithNoStage.copy(hasBoneLesions = true, hasBrainLesions = true)))
            .containsOnly(TumorStage.IV)
    }
}