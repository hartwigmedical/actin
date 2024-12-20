package com.hartwig.actin.molecular.orange

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
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class DriverEventFactoryTest {

    @Test
    fun `Should generate events for variants`() {
        assertThat(DriverEventFactory.variantEvent(variant("BRAF", "p.Val600Glu"))).isEqualTo("BRAF V600E")
        assertThat(DriverEventFactory.variantEvent(variant("BRAF", "p.V600E"))).isEqualTo("BRAF V600E")
        assertThat(DriverEventFactory.variantEvent(variant("BRAF", "V600E"))).isEqualTo("BRAF V600E")

        assertThat(
            DriverEventFactory.variantEvent(
                variant("BRAF", "p.?", "c.1587-5delT", PurpleVariantEffect.SPLICE_ACCEPTOR, PurpleCodingEffect.SPLICE)
            )
        ).isEqualTo("BRAF c.1587-5delT splice")

        assertThat(
            DriverEventFactory.variantEvent(
                variant("BRAF", "", "n.271-7395delT", PurpleVariantEffect.INTRONIC, PurpleCodingEffect.NONE)
            )
        ).isEqualTo("BRAF n.271-7395delT")

        assertThat(
            DriverEventFactory.variantEvent(
                variant("BRAF", "", "", PurpleVariantEffect.UPSTREAM_GENE, PurpleCodingEffect.NONE)
            )
        ).isEqualTo("BRAF upstream")

        assertThat(
            DriverEventFactory.variantEvent(
                variant("BRAF", "", "", PurpleVariantEffect.NON_CODING_TRANSCRIPT, PurpleCodingEffect.NONE)
            )
        ).isEqualTo("BRAF NON_CODING_TRANSCRIPT")
    }

    @Test
    fun `Should generate events for copy numbers`() {
        assertThat(DriverEventFactory.gainLossEvent(gainLoss("MYC", CopyNumberInterpretation.FULL_GAIN))).isEqualTo("MYC amp")
        assertThat(DriverEventFactory.gainLossEvent(gainLoss("MYC", CopyNumberInterpretation.PARTIAL_GAIN))).isEqualTo("MYC amp")
        assertThat(DriverEventFactory.gainLossEvent(gainLoss("PTEN", CopyNumberInterpretation.FULL_LOSS))).isEqualTo("PTEN del")
        assertThat(DriverEventFactory.gainLossEvent(gainLoss("PTEN", CopyNumberInterpretation.PARTIAL_LOSS))).isEqualTo("PTEN del")
    }

    @Test
    fun `Should generate events for disruptions`() {
        assertThat(
            DriverEventFactory.homozygousDisruptionEvent(TestLinxFactory.homozygousDisruptionBuilder().gene("TP53").build())
        ).isEqualTo("TP53 hom disruption")

        assertThat(DriverEventFactory.disruptionEvent(TestLinxFactory.breakendBuilder().gene("TP53").build())).isEqualTo("TP53 disruption")
    }

    @Test
    fun `Should generate events for fusions`() {
        assertThat(
            DriverEventFactory.fusionEvent(TestLinxFactory.fusionBuilder().geneStart("EML4").geneEnd("ALK").build())
        ).isEqualTo("EML4::ALK fusion")
    }

    @Test
    fun `Should generate events for viruses`() {
        assertThat(DriverEventFactory.virusEvent(virus("Papilloma", VirusInterpretation.HPV))).isEqualTo("HPV (Papilloma) positive")
        assertThat(DriverEventFactory.virusEvent(virus("Papilloma", VirusInterpretation.EBV))).isEqualTo("EBV positive")
        assertThat(DriverEventFactory.virusEvent(virus("Papilloma", null))).isEqualTo("Papilloma positive")
    }

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