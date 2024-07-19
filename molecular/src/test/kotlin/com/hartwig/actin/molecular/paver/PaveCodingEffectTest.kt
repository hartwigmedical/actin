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

    @Test
    fun `Should convert variant effect to coding effect`() {
        listOf(PaveVariantEffect.FRAMESHIFT, PaveVariantEffect.STOP_GAINED, PaveVariantEffect.STOP_LOST, PaveVariantEffect.START_LOST)
            .forEach {
                assertThat(PaveCodingEffect.fromPaveVariantEffect(it)).isEqualTo(PaveCodingEffect.NONSENSE_OR_FRAMESHIFT)
            }

        listOf(PaveVariantEffect.SPLICE_ACCEPTOR, PaveVariantEffect.SPLICE_DONOR)
            .forEach {
                assertThat(PaveCodingEffect.fromPaveVariantEffect(it)).isEqualTo(PaveCodingEffect.SPLICE)
            }

        listOf(PaveVariantEffect.MISSENSE, PaveVariantEffect.INFRAME_DELETION, PaveVariantEffect.INFRAME_INSERTION,
            PaveVariantEffect.PHASED_INFRAME_DELETION, PaveVariantEffect.PHASED_INFRAME_INSERTION, PaveVariantEffect.PHASED_MISSENSE)
            .forEach {
                assertThat(PaveCodingEffect.fromPaveVariantEffect(it)).isEqualTo(PaveCodingEffect.MISSENSE)
            }

        listOf(PaveVariantEffect.SYNONYMOUS, PaveVariantEffect.PHASED_SYNONYMOUS)
            .forEach {
                assertThat(PaveCodingEffect.fromPaveVariantEffect(it)).isEqualTo(PaveCodingEffect.SYNONYMOUS)
            }

        listOf(PaveVariantEffect.INTRONIC, PaveVariantEffect.FIVE_PRIME_UTR, PaveVariantEffect.THREE_PRIME_UTR,
            PaveVariantEffect.UPSTREAM_GENE, PaveVariantEffect.NON_CODING_TRANSCRIPT, PaveVariantEffect.OTHER)
            .forEach {
                assertThat(PaveCodingEffect.fromPaveVariantEffect(it)).isEqualTo(PaveCodingEffect.NONE)
            }

    }
}