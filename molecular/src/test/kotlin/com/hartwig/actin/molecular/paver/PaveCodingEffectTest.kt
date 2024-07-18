package com.hartwig.actin.molecular.paver

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class PaveCodingEffectTest {
    @Test
    fun `Should determine worst coding effect`() {
        assertThat(PaveCodingEffect.worstCodingEffect(
            listOf(PaveCodingEffect.NONE, PaveCodingEffect.MISSENSE, PaveCodingEffect.SYNONYMOUS)
        )).isEqualTo(PaveCodingEffect.MISSENSE);
    }
}