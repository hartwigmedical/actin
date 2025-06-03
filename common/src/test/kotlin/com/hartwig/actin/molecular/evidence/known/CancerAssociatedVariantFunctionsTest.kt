package com.hartwig.actin.molecular.evidence.known

import com.hartwig.serve.datamodel.Knowledgebase
import com.hartwig.serve.datamodel.molecular.common.ProteinEffect
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class CancerAssociatedVariantFunctionsTest {

    @Test
    fun `Should determine cancer-associated variant from gene alteration`() {
        assertThat(
            CancerAssociatedVariantFunctions.isCancerAssociatedVariant(
                TestServeKnownFactory.hotspotBuilder()
                    .proteinEffect(ProteinEffect.GAIN_OF_FUNCTION)
                    .sources(setOf(Knowledgebase.CKB))
                    .build()
            )
        ).isTrue()

        assertThat(
            CancerAssociatedVariantFunctions.isCancerAssociatedVariant(
                TestServeKnownFactory.hotspotBuilder()
                    .proteinEffect(ProteinEffect.NO_EFFECT)
                    .sources(setOf(Knowledgebase.CKB))
                    .build()
            )
        ).isFalse()

        assertThat(
            CancerAssociatedVariantFunctions.isCancerAssociatedVariant(
                TestServeKnownFactory.codonBuilder()
                    .proteinEffect(ProteinEffect.LOSS_OF_FUNCTION)
                    .sources(setOf(Knowledgebase.CKB))
                    .build()
            )
        ).isTrue()

        assertThat(
            CancerAssociatedVariantFunctions.isCancerAssociatedVariant(
                TestServeKnownFactory.exonBuilder()
                    .proteinEffect(ProteinEffect.GAIN_OF_FUNCTION)
                    .sources(setOf(Knowledgebase.CKB))
                    .build()
            )
        ).isFalse()

        assertThat(
            CancerAssociatedVariantFunctions.isCancerAssociatedVariant(
                TestServeKnownFactory.hotspotBuilder()
                    .sources(setOf(Knowledgebase.DOCM))
                    .build()
            )
        ).isTrue
        assertThat(CancerAssociatedVariantFunctions.isCancerAssociatedVariant(null)).isFalse()
    }
}