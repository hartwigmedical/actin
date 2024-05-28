package com.hartwig.actin.molecular.evidence.actionability

import com.hartwig.actin.molecular.datamodel.CodingEffect
import com.hartwig.actin.molecular.evidence.matching.VARIANT_CRITERIA
import com.hartwig.serve.datamodel.ActionableEvents
import com.hartwig.serve.datamodel.ImmutableActionableEvents
import com.hartwig.serve.datamodel.MutationType
import com.hartwig.serve.datamodel.gene.ActionableGene
import com.hartwig.serve.datamodel.gene.GeneEvent
import com.hartwig.serve.datamodel.hotspot.ActionableHotspot
import com.hartwig.serve.datamodel.range.ImmutableActionableRange
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExhaustiveVariantEvidenceTest {

    @Test
    fun `Should determine evidence for hotpots`() {
        val hotspot1: ActionableHotspot =
            TestServeActionabilityFactory.hotspotBuilder().gene("gene 1").chromosome("X").position(2).ref("A").alt("G").build()
        val hotspot2: ActionableHotspot =
            TestServeActionabilityFactory.hotspotBuilder().gene("gene 2").chromosome("X").position(2).ref("A").alt("G").build()
        val hotspot3: ActionableHotspot =
            TestServeActionabilityFactory.hotspotBuilder().gene("gene 1").chromosome("X").position(2).ref("A").alt("C").build()
        val actionable: ActionableEvents = ImmutableActionableEvents.builder().addAllHotspots(listOf(hotspot1, hotspot2, hotspot3)).build()
        val variantEvidence: VariantEvidence = VariantEvidence.create(actionable)

        val variantGene1 = VARIANT_CRITERIA.copy(gene = "gene 1", chromosome = "X", position = 2, ref = "A", alt = "G", isReportable = true)
        val matchesVariant1 = variantEvidence.findMatches(variantGene1)
        assertEquals(1, matchesVariant1.size.toLong())
        assertTrue(matchesVariant1.contains(hotspot1))

        val variantGene2 = VARIANT_CRITERIA.copy(gene = "gene 2", chromosome = "X", position = 2, ref = "A", alt = "G", isReportable = true)
        val matchesVariant2 = variantEvidence.findMatches(variantGene2)
        assertEquals(1, matchesVariant2.size.toLong())
        assertTrue(matchesVariant2.contains(hotspot2))

        val otherVariantGene1 =
            VARIANT_CRITERIA.copy(gene = "gene 1", chromosome = "X", position = 2, ref = "A", alt = "T", isReportable = true)
        assertTrue(variantEvidence.findMatches(otherVariantGene1).isEmpty())
    }

    @Test
    fun shouldDetermineEvidenceForCodons() {
        assertEvidenceDeterminedForRange(ImmutableActionableEvents.builder().addCodons(ACTIONABLE_RANGE).build())
    }

    @Test
    fun shouldDetermineEvidenceForExons() {
        assertEvidenceDeterminedForRange(ImmutableActionableEvents.builder().addExons(ACTIONABLE_RANGE).build())
    }

    private fun assertEvidenceDeterminedForRange(actionable: ActionableEvents) {
        val variantEvidence: VariantEvidence = VariantEvidence.create(actionable)

        val variantGene1 = VARIANT_CRITERIA.copy(
            gene = "gene 1",
            chromosome = "X",
            position = 6,
            isReportable = true,
            codingEffect = CodingEffect.MISSENSE
        )
        val matchesVariant1 = variantEvidence.findMatches(variantGene1)
        assertEquals(1, matchesVariant1.size.toLong())
        assertTrue(matchesVariant1.contains(ACTIONABLE_RANGE))

        val otherVariantGene1 = VARIANT_CRITERIA.copy(
            gene = "gene 1",
            chromosome = "X",
            position = 2,
            isReportable = true,
            codingEffect = CodingEffect.MISSENSE
        )
        assertTrue(variantEvidence.findMatches(otherVariantGene1).isEmpty())
    }

    @Test
    fun shouldDetermineEvidenceForGenes() {
        val gene1: ActionableGene = TestServeActionabilityFactory.geneBuilder().gene("gene 1").event(GeneEvent.ANY_MUTATION).build()
        val gene2: ActionableGene = TestServeActionabilityFactory.geneBuilder().gene("gene 2").event(GeneEvent.ACTIVATION).build()
        val gene3: ActionableGene = TestServeActionabilityFactory.geneBuilder().gene("gene 2").event(GeneEvent.AMPLIFICATION).build()
        val actionable: ActionableEvents = ImmutableActionableEvents.builder().addGenes(gene1, gene2, gene3).build()
        val variantEvidence: VariantEvidence = VariantEvidence.create(actionable)

        val variantGene1 = VARIANT_CRITERIA.copy(
            gene = "gene 1",
            codingEffect = CodingEffect.MISSENSE,
            isReportable = true
        )
        val matchesVariant1 = variantEvidence.findMatches(variantGene1)
        assertEquals(1, matchesVariant1.size.toLong())
        assertTrue(matchesVariant1.contains(gene1))

        val variantGene2 = VARIANT_CRITERIA.copy(
            gene = "gene 2",
            codingEffect = CodingEffect.MISSENSE,
            isReportable = true
        )
        val matchesVariant2 = variantEvidence.findMatches(variantGene2)
        assertEquals(1, matchesVariant2.size.toLong())
        assertTrue(matchesVariant2.contains(gene2))
    }

    companion object {
        val ACTIONABLE_RANGE: ImmutableActionableRange = TestServeActionabilityFactory.rangeBuilder()
            .gene("gene 1")
            .chromosome("X")
            .start(4)
            .end(8)
            .applicableMutationType(MutationType.ANY)
            .build()
    }
}