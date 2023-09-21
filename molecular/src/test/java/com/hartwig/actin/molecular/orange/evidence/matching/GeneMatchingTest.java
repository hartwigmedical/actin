package com.hartwig.actin.molecular.orange.evidence.matching;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.hartwig.hmftools.datamodel.purple.PurpleCodingEffect;
import com.hartwig.hmftools.datamodel.purple.PurpleVariant;
import com.hartwig.actin.molecular.orange.datamodel.purple.TestPurpleFactory;
import com.hartwig.serve.datamodel.gene.GeneAnnotation;
import com.hartwig.serve.datamodel.gene.GeneEvent;

import org.jetbrains.annotations.NotNull;
import org.junit.Test;

public class GeneMatchingTest {

    @Test
    public void canMatchGenes() {
        GeneAnnotation annotation = createAnnotation("gene 1");

        PurpleVariant match = createVariant("gene 1", PurpleCodingEffect.MISSENSE);
        assertTrue(GeneMatching.isMatch(annotation, match));

        PurpleVariant wrongGene = createVariant("gene 2", PurpleCodingEffect.MISSENSE);
        assertFalse(GeneMatching.isMatch(annotation, wrongGene));

        PurpleVariant nonCoding = createVariant("gene 1", PurpleCodingEffect.NONE);
        assertFalse(GeneMatching.isMatch(annotation, nonCoding));
    }

    @NotNull
    private static PurpleVariant createVariant(@NotNull String gene, @NotNull PurpleCodingEffect codingEffect) {
        return TestPurpleFactory.variantBuilder()
                .gene(gene)
                .canonicalImpact(TestPurpleFactory.transcriptImpactBuilder().codingEffect(codingEffect).build())
                .build();
    }

    @NotNull
    private static GeneAnnotation createAnnotation(@NotNull String gene) {
        return new GeneAnnotation() {
            @NotNull
            @Override
            public String gene() {
                return gene;
            }

            @NotNull
            @Override
            public GeneEvent event() {
                return GeneEvent.ANY_MUTATION;
            }
        };
    }
}