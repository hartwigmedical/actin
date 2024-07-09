package com.hartwig.actin.molecular.orange.interpretation

import com.hartwig.actin.molecular.orange.datamodel.linx.TestLinxFactory
import com.hartwig.actin.molecular.orange.datamodel.purple.TestPurpleFactory
import com.hartwig.actin.molecular.orange.datamodel.virus.TestVirusInterpreterFactory
import com.hartwig.hmftools.datamodel.purple.CopyNumberInterpretation
import com.hartwig.hmftools.datamodel.purple.PurpleCodingEffect
import com.hartwig.hmftools.datamodel.purple.PurpleGainLoss
import com.hartwig.hmftools.datamodel.purple.PurpleVariant
import com.hartwig.hmftools.datamodel.purple.PurpleVariantEffect
import com.hartwig.hmftools.datamodel.virus.VirusInterpretation
import com.hartwig.hmftools.datamodel.virus.VirusInterpreterEntry
import org.junit.Assert.assertEquals
import org.junit.Test

class DriverEventFactoryTest {

    @Test
    fun canGenerateEventsForVariants() {
        assertEquals("BRAF V600E", DriverEventFactory.variantEvent(variant("BRAF", "p.Val600Glu")))
        assertEquals("BRAF V600E", DriverEventFactory.variantEvent(variant("BRAF", "p.V600E")))
        assertEquals("BRAF V600E", DriverEventFactory.variantEvent(variant("BRAF", "V600E")))

        assertEquals(
            "BRAF c.1587-5delT splice",
            DriverEventFactory.variantEvent(
                variant(
                    "BRAF",
                    "p.?",
                    "c.1587-5delT",
                    PurpleVariantEffect.SPLICE_ACCEPTOR,
                    PurpleCodingEffect.SPLICE
                )
            )
        )

        assertEquals(
            "BRAF n.271-7395delT",
            DriverEventFactory.variantEvent(
                variant(
                    "BRAF",
                    "",
                    "n.271-7395delT",
                    PurpleVariantEffect.INTRONIC,
                    PurpleCodingEffect.NONE
                )
            )
        )

        assertEquals(
            "BRAF upstream",
            DriverEventFactory.variantEvent(
                variant(
                    "BRAF",
                    "",
                    "",
                    PurpleVariantEffect.UPSTREAM_GENE,
                    PurpleCodingEffect.NONE
                )
            )
        )

        assertEquals(
            "BRAF NON_CODING_TRANSCRIPT",
            DriverEventFactory.variantEvent(
                variant(
                    "BRAF",
                    "",
                    "",
                    PurpleVariantEffect.NON_CODING_TRANSCRIPT,
                    PurpleCodingEffect.NONE
                )
            )
        )
    }

    @Test
    fun canGenerateEventsForCopyNumbers() {
        assertEquals("MYC amp", DriverEventFactory.gainLossEvent(gainLoss("MYC", CopyNumberInterpretation.FULL_GAIN)))
        assertEquals("MYC amp", DriverEventFactory.gainLossEvent(gainLoss("MYC", CopyNumberInterpretation.PARTIAL_GAIN)))
        assertEquals("PTEN del", DriverEventFactory.gainLossEvent(gainLoss("PTEN", CopyNumberInterpretation.FULL_LOSS)))
        assertEquals("PTEN del", DriverEventFactory.gainLossEvent(gainLoss("PTEN", CopyNumberInterpretation.PARTIAL_LOSS)))
    }

    @Test
    fun canGenerateEventsForDisruptions() {
        assertEquals(
            "TP53 hom disruption",
            DriverEventFactory.homozygousDisruptionEvent(TestLinxFactory.homozygousDisruptionBuilder().gene("TP53").build())
        )
        assertEquals("TP53 disruption", DriverEventFactory.disruptionEvent(TestLinxFactory.breakendBuilder().gene("TP53").build()))
    }

    @Test
    fun canGenerateEventsForFusions() {
        assertEquals(
            "EML4 - ALK fusion",
            DriverEventFactory.fusionEvent(TestLinxFactory.fusionBuilder().geneStart("EML4").geneEnd("ALK").build())
        )
    }

    @Test
    fun canGenerateEventsForViruses() {
        assertEquals("HPV (Papilloma) positive", DriverEventFactory.virusEvent(virus("Papilloma", VirusInterpretation.HPV)))
        assertEquals("EBV positive", DriverEventFactory.virusEvent(virus("Papilloma", VirusInterpretation.EBV)))
        assertEquals("Papilloma positive", DriverEventFactory.virusEvent(virus("Papilloma", null)))
    }

    companion object {
        private fun variant(gene: String, hgvsProteinImpact: String): PurpleVariant {
            return TestPurpleFactory.variantBuilder()
                .gene(gene)
                .canonicalImpact(TestPurpleFactory.transcriptImpactBuilder().hgvsProteinImpact(hgvsProteinImpact).build())
                .build()
        }

        private fun variant(
            gene: String, hgvsProteinImpact: String, hgvsCodingImpact: String,
            effect: PurpleVariantEffect, codingEffect: PurpleCodingEffect
        ): PurpleVariant {
            return TestPurpleFactory.variantBuilder()
                .gene(gene)
                .canonicalImpact(
                    TestPurpleFactory.transcriptImpactBuilder()
                        .hgvsProteinImpact(hgvsProteinImpact)
                        .hgvsCodingImpact(hgvsCodingImpact)
                        .addEffects(effect)
                        .codingEffect(codingEffect)
                        .build()
                )
                .build()
        }

        private fun gainLoss(gene: String, interpretation: CopyNumberInterpretation): PurpleGainLoss {
            return TestPurpleFactory.gainLossBuilder().gene(gene).interpretation(interpretation).build()
        }

        private fun virus(name: String, interpretation: VirusInterpretation?): VirusInterpreterEntry {
            return TestVirusInterpreterFactory.builder().name(name).interpretation(interpretation).build()
        }
    }
}