package com.hartwig.actin.molecular.hotspot

import com.hartwig.actin.molecular.evidence.known.TestServeKnownFactory
import com.hartwig.serve.datamodel.molecular.common.ProteinEffect
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class HotspotFunctionsTest {

    @Test
    fun `Should determine serve hotspot status`() {
        val gainOfFunction = TestServeKnownFactory.hotspotBuilder().proteinEffect(ProteinEffect.GAIN_OF_FUNCTION).build()
        val unknownFunction = TestServeKnownFactory.hotspotBuilder().proteinEffect(ProteinEffect.UNKNOWN).build()
        val noHotspot = TestServeKnownFactory.copyNumberBuilder().proteinEffect(ProteinEffect.GAIN_OF_FUNCTION).build()

        assertThat(HotspotFunctions.isHotspot(gainOfFunction)).isTrue()
        assertThat(HotspotFunctions.isHotspot(unknownFunction)).isFalse()
        assertThat(HotspotFunctions.isHotspot(noHotspot)).isFalse()
    }
}