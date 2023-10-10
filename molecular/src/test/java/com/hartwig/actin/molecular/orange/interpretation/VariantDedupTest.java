package com.hartwig.actin.molecular.orange.interpretation;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;
import java.util.Set;

import com.hartwig.actin.molecular.orange.datamodel.purple.TestPurpleFactory;
import com.hartwig.hmftools.datamodel.purple.PurpleVariant;
import com.hartwig.hmftools.datamodel.purple.PurpleVariantEffect;

import org.junit.Test;

public class VariantDedupTest {

    @Test
    public void shouldWorkOnEmptySetOfVariants() {
        Set<PurpleVariant> dedup = VariantDedup.apply(Collections.emptySet());
        assertThat(dedup).isEmpty();
    }

    @Test
    public void shouldDedupVariantsThatAreInterpretedAsPhasedInframe() {
        PurpleVariant variant1 = TestPurpleFactory.variantBuilder()
                .gene("EGFR")
                .canonicalImpact(TestPurpleFactory.transcriptImpactBuilder()
                        .hgvsCodingImpact("c.1")
                        .hgvsProteinImpact("p.Glu746_Pro753delinsMetSer")
                        .addEffects(PurpleVariantEffect.PHASED_INFRAME_DELETION)
                        .build())
                .variantCopyNumber(0.9)
                .build();

        PurpleVariant variant2 = TestPurpleFactory.variantBuilder()
                .from(variant1)
                .canonicalImpact(TestPurpleFactory.transcriptImpactBuilder()
                        .from(variant1.canonicalImpact())
                        .hgvsCodingImpact("c.2")
                        .build())
                .variantCopyNumber(1.2)
                .build();

        PurpleVariant variant3 = TestPurpleFactory.variantBuilder()
                .from(variant1)
                .canonicalImpact(TestPurpleFactory.transcriptImpactBuilder()
                        .from(variant1.canonicalImpact())
                        .hgvsCodingImpact("c.3")
                        .build())
                .variantCopyNumber(0.9)
                .build();

        PurpleVariant variant4 = TestPurpleFactory.variantBuilder()
                .gene("APC")
                .canonicalImpact(TestPurpleFactory.transcriptImpactBuilder()
                        .hgvsProteinImpact("p.Met1fs")
                        .addEffects(PurpleVariantEffect.FRAMESHIFT)
                        .build())
                .variantCopyNumber(0.8)
                .build();

        Set<PurpleVariant> variants = Set.of(variant1, variant2, variant3, variant4);
        Set<PurpleVariant> dedup = VariantDedup.apply(variants);

        assertThat(dedup.size()).isEqualTo(2);
        assertThat(dedup).contains(variant3);
        assertThat(dedup).contains(variant4);
    }

    @Test
    public void shouldNotDedupUnrelatedVariants() {
        PurpleVariant variant1 = TestPurpleFactory.variantBuilder().gene("gene 1").build();
        PurpleVariant variant2 = TestPurpleFactory.variantBuilder().gene("gene 2").build();

        Set<PurpleVariant> variants = Set.of(variant1, variant2);
        Set<PurpleVariant> dedup = VariantDedup.apply(variants);

        assertThat(dedup.size()).isEqualTo(2);
        assertThat(dedup).contains(variant1);
        assertThat(dedup).contains(variant2);
    }
}