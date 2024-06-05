package com.hartwig.actin.molecular.orange.interpretation

import com.hartwig.actin.molecular.orange.datamodel.purple.TestPurpleFactory
import com.hartwig.hmftools.datamodel.purple.PurpleVariant
import com.hartwig.hmftools.datamodel.purple.PurpleVariantEffect
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class ExtendedVariantDedupTest {

    @Test
    fun shouldWorkOnEmptySetOfVariants() {
        val dedup = VariantDedup.apply(mutableSetOf())
        assertThat(dedup).isEmpty()
    }

    @Test
    fun shouldDedupVariantsThatAreInterpretedAsPhasedInframe() {
        val variant1: PurpleVariant = TestPurpleFactory.variantBuilder()
            .gene("EGFR")
            .canonicalImpact(TestPurpleFactory.transcriptImpactBuilder()
                .hgvsCodingImpact("c.1")
                .hgvsProteinImpact("p.Glu746_Pro753delinsMetSer")
                .addEffects(PurpleVariantEffect.PHASED_INFRAME_DELETION)
                .build())
            .variantCopyNumber(0.9)
            .build()
        val variant2: PurpleVariant = TestPurpleFactory.variantBuilder()
            .from(variant1)
            .canonicalImpact(TestPurpleFactory.transcriptImpactBuilder()
                .from(variant1.canonicalImpact())
                .hgvsCodingImpact("c.2")
                .build())
            .variantCopyNumber(1.2)
            .build()
        val variant3: PurpleVariant = TestPurpleFactory.variantBuilder()
            .from(variant1)
            .canonicalImpact(TestPurpleFactory.transcriptImpactBuilder()
                .from(variant1.canonicalImpact())
                .hgvsCodingImpact("c.3")
                .build())
            .variantCopyNumber(0.9)
            .build()
        val variant4: PurpleVariant = TestPurpleFactory.variantBuilder()
            .gene("APC")
            .canonicalImpact(TestPurpleFactory.transcriptImpactBuilder()
                .hgvsProteinImpact("p.Met1fs")
                .addEffects(PurpleVariantEffect.FRAMESHIFT)
                .build())
            .variantCopyNumber(0.8)
            .build()
        val variants = setOf(variant1, variant2, variant3, variant4)

        val dedup = VariantDedup.apply(variants)
        assertThat(dedup.size).isEqualTo(2)
        assertThat(dedup).contains(variant3)
        assertThat(dedup).contains(variant4)
    }

    @Test
    fun shouldNotDedupUnrelatedVariants() {
        val variant1: PurpleVariant = TestPurpleFactory.variantBuilder().gene("gene 1").build()
        val variant2: PurpleVariant = TestPurpleFactory.variantBuilder().gene("gene 2").build()
        val variants = setOf(variant1, variant2)

        val dedup = VariantDedup.apply(variants)
        assertThat(dedup.size).isEqualTo(2)
        assertThat(dedup).contains(variant1)
        assertThat(dedup).contains(variant2)
    }
}