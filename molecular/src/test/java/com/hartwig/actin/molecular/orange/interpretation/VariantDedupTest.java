package com.hartwig.actin.molecular.orange.interpretation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Set;

import com.hartwig.actin.molecular.orange.datamodel.purple.PurpleVariant;
import com.hartwig.actin.molecular.orange.datamodel.purple.PurpleVariantEffect;
import com.hartwig.actin.molecular.orange.datamodel.purple.TestPurpleFactory;

import org.junit.Test;

public class VariantDedupTest {

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

        assertEquals(2, dedup.size());
        assertTrue(dedup.contains(variant3));
        assertTrue(dedup.contains(variant4));
    }
}