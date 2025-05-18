package com.hartwig.actin.molecular.evidence.known

import com.hartwig.serve.datamodel.Knowledgebase
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
                    .sources(setOf(Knowledgebase.CKB))
                    .build()
            )
        ).isTrue()

        assertThat(
            HotspotFunctions.isHotspot(
                TestServeKnownFactory.hotspotBuilder()
                    .proteinEffect(ProteinEffect.NO_EFFECT)
                    .sources(setOf(Knowledgebase.CKB))
                    .build()
            )
        ).isFalse()

        assertThat(
            HotspotFunctions.isHotspot(
                TestServeKnownFactory.codonBuilder()
                    .proteinEffect(ProteinEffect.LOSS_OF_FUNCTION)
                    .sources(setOf(Knowledgebase.CKB))
                    .build()
            )
        ).isTrue()

        assertThat(
            HotspotFunctions.isHotspot(
                TestServeKnownFactory.exonBuilder()
                    .proteinEffect(ProteinEffect.GAIN_OF_FUNCTION)
                    .sources(setOf(Knowledgebase.CKB))
                    .build()
            )
        ).isFalse()

        assertThat(
            HotspotFunctions.isHotspot(
                TestServeKnownFactory.hotspotBuilder()
                    .sources(setOf(Knowledgebase.DOCM))
                    .build()
            )
        ).isTrue
        assertThat(HotspotFunctions.isHotspot(null)).isFalse()
    }
}