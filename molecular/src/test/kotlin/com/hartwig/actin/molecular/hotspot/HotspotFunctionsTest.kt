package com.hartwig.actin.molecular.hotspot

import com.hartwig.actin.molecular.evidence.known.HotspotFunctions
import com.hartwig.actin.molecular.evidence.known.TestServeKnownFactory
import com.hartwig.serve.datamodel.molecular.common.ProteinEffect
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class HotspotFunctionsTest {

    @Test
    fun `Should determine hotspot from gene alteration`() {
        assertThat(
            HotspotFunctions.isHotspot(
                TestServeKnownFactory.hotspotBuilder()
                    .proteinEffect(ProteinEffect.GAIN_OF_FUNCTION)
                    .build()
            )
        ).isTrue()

        assertThat(
            HotspotFunctions.isHotspot(
                TestServeKnownFactory.hotspotBuilder()
                    .proteinEffect(ProteinEffect.NO_EFFECT)
                    .build()
            )
        ).isFalse()

        assertThat(
            HotspotFunctions.isHotspot(
                TestServeKnownFactory.codonBuilder()
                    .proteinEffect(ProteinEffect.LOSS_OF_FUNCTION)
                    .build()
            )
        ).isTrue()

        assertThat(
            HotspotFunctions.isHotspot(
                TestServeKnownFactory.exonBuilder()
                    .proteinEffect(ProteinEffect.GAIN_OF_FUNCTION)
                    .build()
            )
        ).isFalse()

        assertThat(HotspotFunctions.isHotspot(null)).isFalse()
    }
}