package com.hartwig.actin.molecular.orange.interpretation;

import static org.junit.Assert.assertEquals;

import com.hartwig.actin.molecular.orange.datamodel.linx.TestLinxFactory;
import com.hartwig.actin.molecular.orange.datamodel.purple.PurpleCodingEffect;
import com.hartwig.actin.molecular.orange.datamodel.purple.PurpleCopyNumber;
import com.hartwig.actin.molecular.orange.datamodel.purple.PurpleVariant;
import com.hartwig.actin.molecular.orange.datamodel.purple.PurpleVariantEffect;
import com.hartwig.actin.molecular.orange.datamodel.purple.TestPurpleFactory;
import com.hartwig.actin.molecular.orange.datamodel.virus.TestVirusInterpreterFactory;
import com.hartwig.actin.molecular.orange.datamodel.virus.VirusInterpretation;
import com.hartwig.actin.molecular.orange.datamodel.virus.VirusInterpreterEntry;

import org.apache.logging.log4j.util.Strings;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.Test;

public class DriverEventFactoryTest {

    @Test
    public void canGenerateEventsForVariants() {
        assertEquals("BRAF V600E", DriverEventFactory.variantEvent(variant("BRAF", "p.Val600Glu")));
        assertEquals("BRAF V600E", DriverEventFactory.variantEvent(variant("BRAF", "p.V600E")));
        assertEquals("BRAF V600E", DriverEventFactory.variantEvent(variant("BRAF", "V600E")));

        assertEquals("BRAF c.1587-5delT splice",
                DriverEventFactory.variantEvent(variant("BRAF",
                        "p.?",
                        "c.1587-5delT",
                        PurpleVariantEffect.SPLICE_ACCEPTOR,
                        PurpleCodingEffect.SPLICE)));

        assertEquals("BRAF n.271-7395delT",
                DriverEventFactory.variantEvent(variant("BRAF",
                        Strings.EMPTY,
                        "n.271-7395delT",
                        PurpleVariantEffect.INTRONIC,
                        PurpleCodingEffect.NONE)));

        assertEquals("BRAF upstream",
                DriverEventFactory.variantEvent(variant("BRAF",
                        Strings.EMPTY,
                        Strings.EMPTY,
                        PurpleVariantEffect.UPSTREAM_GENE,
                        PurpleCodingEffect.NONE)));

        assertEquals("BRAF NON_CODING_TRANSCRIPT",
                DriverEventFactory.variantEvent(variant("BRAF",
                        Strings.EMPTY,
                        Strings.EMPTY,
                        PurpleVariantEffect.NON_CODING_TRANSCRIPT,
                        PurpleCodingEffect.NONE)));
    }

    @Test
    public void canGenerateEventsForCopyNumbers() {
        assertEquals("MYC amp", DriverEventFactory.amplificationEvent(copyNumber("MYC")));
        assertEquals("PTEN del", DriverEventFactory.lossEvent(copyNumber("PTEN")));
    }

    @Test
    public void canGenerateEventsForDisruptions() {
        assertEquals("TP53 hom disruption",
                DriverEventFactory.homozygousDisruptionEvent(TestLinxFactory.homozygousDisruptionBuilder().gene("TP53").build()));
        assertEquals("TP53 disruption", DriverEventFactory.disruptionEvent(TestLinxFactory.breakendBuilder().gene("TP53").build()));
    }

    @Test
    public void canGenerateEventsForFusions() {
        assertEquals("EML4 - ALK fusion",
                DriverEventFactory.fusionEvent(TestLinxFactory.fusionBuilder().geneStart("EML4").geneEnd("ALK").build()));
    }

    @Test
    public void canGenerateEventsForViruses() {
        assertEquals("HPV positive", DriverEventFactory.virusEvent(virus("Papilloma", VirusInterpretation.HPV)));
        assertEquals("Papilloma positive", DriverEventFactory.virusEvent(virus("Papilloma", null)));
    }

    @NotNull
    private static PurpleVariant variant(@NotNull String gene, @NotNull String hgvsProteinImpact) {
        return TestPurpleFactory.variantBuilder()
                .gene(gene)
                .canonicalImpact(TestPurpleFactory.transcriptImpactBuilder().hgvsProteinImpact(hgvsProteinImpact).build())
                .build();
    }

    @NotNull
    private static PurpleVariant variant(@NotNull String gene, @NotNull String hgvsProteinImpact, @NotNull String hgvsCodingImpact,
            @NotNull PurpleVariantEffect effect, @NotNull PurpleCodingEffect codingEffect) {
        return TestPurpleFactory.variantBuilder()
                .gene(gene)
                .canonicalImpact(TestPurpleFactory.transcriptImpactBuilder()
                        .hgvsProteinImpact(hgvsProteinImpact)
                        .hgvsCodingImpact(hgvsCodingImpact)
                        .addEffects(effect)
                        .codingEffect(codingEffect)
                        .build())
                .build();
    }

    @NotNull
    private static PurpleCopyNumber copyNumber(@NotNull String gene) {
        return TestPurpleFactory.copyNumberBuilder().gene(gene).build();
    }

    @NotNull
    private static VirusInterpreterEntry virus(@NotNull String name, @Nullable VirusInterpretation interpretation) {
        return TestVirusInterpreterFactory.builder().name(name).interpretation(interpretation).build();
    }
}