package com.hartwig.actin.algo.evaluation.tumor

import com.hartwig.actin.algo.doid.DoidConstants
import com.hartwig.actin.clinical.datamodel.ImmutableTumorDetails
import com.hartwig.actin.clinical.datamodel.TumorStage
import com.hartwig.actin.doid.TestDoidModelFactory
import org.assertj.core.api.Assertions
import org.junit.Before
import org.junit.Test

class TumorStageDerivationFunctionTest {
    private var victim: TumorStageDerivationFunction? = null

    @Before
    fun setUp() {
        victim = TumorStageDerivationFunction.create(TestDoidModelFactory.createMinimalTestDoidModel())
    }

    @Test
    fun shouldReturnEmptySetOfDerivationWhenNoDoidsConfigured() {
        Assertions.assertThat(victim!!.apply(tumorBuilderWithNoStage().build())).isEmpty()
    }

    @Test
    fun shouldReturnEmptySetOfDerivationWhenNoLesionDetailsConfigured() {
        Assertions.assertThat(victim!!.apply(tumorBuilderWithNoStage().build())).isEmpty()
    }

    @Test
    fun shouldReturnStageIAndIIWhenNoLesions() {
        Assertions.assertThat(
            victim!!.apply(
                tumorBuilderWithNoStage().hasBoneLesions(false)
                    .hasBrainLesions(false)
                    .hasLiverLesions(false)
                    .hasLymphNodeLesions(false)
                    .hasBoneLesions(false)
                    .hasCnsLesions(false)
                    .hasLungLesions(false)
                    .build()
            )
        ).containsOnly(TumorStage.I, TumorStage.II)
    }

    @Test
    fun shouldReturnStageIIIAndIVWhenOneCategorizedLocation() {
        Assertions.assertThat(victim!!.apply(tumorBuilderWithNoStage().hasLymphNodeLesions(true).build()))
            .containsOnly(TumorStage.III, TumorStage.IV)
    }

    @Test
    fun shouldReturnStageIVWhenMultipleLesions() {
        Assertions.assertThat(victim!!.apply(tumorBuilderWithNoStage().hasBoneLesions(true).hasBrainLesions(true).build()))
            .containsOnly(TumorStage.IV)
    }

    companion object {
        private fun tumorBuilderWithNoStage(): ImmutableTumorDetails.Builder {
            return TumorTestFactory.builder().stage(null).doids(listOf(DoidConstants.BREAST_CANCER_DOID))
        }
    }
}