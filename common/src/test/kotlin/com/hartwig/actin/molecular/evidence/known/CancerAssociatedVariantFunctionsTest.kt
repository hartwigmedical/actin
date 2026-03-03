package com.hartwig.actin.molecular.evidence.known

import com.hartwig.serve.datamodel.Knowledgebase
import com.hartwig.serve.datamodel.molecular.common.ProteinEffect
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class CancerAssociatedVariantFunctionsTest {

    @Test
    fun `Should determine cancer-associated variant from gene alteration`() {
        assertThat(
            CancerAssociatedVariantFunctions.isAssociatedWithCancer(
                TestServeKnownFactory.hotspotBuilder()
                    .proteinEffect(ProteinEffect.GAIN_OF_FUNCTION)
                    .sources(setOf(Knowledgebase.CKB))
                    .build()
            )
        ).isTrue()

        assertThat(
            CancerAssociatedVariantFunctions.isAssociatedWithCancer(
                TestServeKnownFactory.hotspotBuilder()
                    .proteinEffect(ProteinEffect.NO_EFFECT)
                    .sources(setOf(Knowledgebase.CKB))
                    .build()
            )
        ).isFalse()

        assertThat(
            CancerAssociatedVariantFunctions.isAssociatedWithCancer(
                TestServeKnownFactory.codonBuilder()
                    .proteinEffect(ProteinEffect.LOSS_OF_FUNCTION)
                    .sources(setOf(Knowledgebase.CKB))
                    .build()
            )
        ).isTrue()

        assertThat(
            CancerAssociatedVariantFunctions.isAssociatedWithCancer(
                TestServeKnownFactory.exonBuilder()
                    .proteinEffect(ProteinEffect.GAIN_OF_FUNCTION)
                    .sources(setOf(Knowledgebase.CKB))
                    .build()
            )
        ).isFalse()

        assertThat(
            CancerAssociatedVariantFunctions.isAssociatedWithCancer(
                TestServeKnownFactory.hotspotBuilder()
                    .sources(setOf(Knowledgebase.DOCM))
                    .build()
            )
        ).isTrue
        assertThat(CancerAssociatedVariantFunctions.isAssociatedWithCancer(null)).isFalse()
    }
}