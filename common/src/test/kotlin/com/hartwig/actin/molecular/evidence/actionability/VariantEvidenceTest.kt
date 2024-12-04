package com.hartwig.actin.molecular.evidence.actionability

import com.hartwig.actin.datamodel.molecular.CodingEffect
import com.hartwig.actin.molecular.evidence.TestServeEvidenceFactory
import com.hartwig.actin.molecular.evidence.TestServeMolecularFactory
import com.hartwig.actin.molecular.evidence.matching.VARIANT_CRITERIA
import com.hartwig.serve.datamodel.efficacy.EfficacyEvidence
import com.hartwig.serve.datamodel.molecular.MutationType
import com.hartwig.serve.datamodel.molecular.gene.GeneEvent
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class VariantEvidenceTest {

    private val actionableCodon: EfficacyEvidence = TestServeEvidenceFactory.create(
        molecularCriterium = TestServeMolecularFactory.createCodon(
            gene = "gene 1",
            chromosome = "X",
            start = 4,
            end = 8,
            applicableMutationType = MutationType.ANY
        )
    )

    private val actionableExon: EfficacyEvidence = TestServeEvidenceFactory.create(
        molecularCriterium = TestServeMolecularFactory.createExon(
            gene = "gene 1",
            chromosome = "X",
            start = 4,
            end = 8,
            applicableMutationType = MutationType.ANY
        )
    )

    @Test
    fun `Should determine evidence for hotpots`() {
        val hotspot1 = TestServeEvidenceFactory.createEvidenceForHotspot("gene 1", "X", 2, "A", "G")
        val hotspot2 = TestServeEvidenceFactory.createEvidenceForHotspot("gene 2", "X", 2, "A", "G")
        val hotspot3 = TestServeEvidenceFactory.createEvidenceForHotspot("gene 1", "X", 2, "A", "C")
        val variantEvidence = VariantEvidence.create(evidences = listOf(hotspot1, hotspot2, hotspot3), trials = emptyList())

        val variantGene1 = VARIANT_CRITERIA.copy(gene = "gene 1", chromosome = "X", position = 2, ref = "A", alt = "G", isReportable = true)
        val matchesVariant1 = variantEvidence.findMatches(variantGene1)
        assertThat(matchesVariant1.evidenceMatches.size).isEqualTo(1)
        assertThat(matchesVariant1.evidenceMatches).contains(hotspot1)

        val variantGene2 = VARIANT_CRITERIA.copy(gene = "gene 2", chromosome = "X", position = 2, ref = "A", alt = "G", isReportable = true)
        val matchesVariant2 = variantEvidence.findMatches(variantGene2)
        assertThat(matchesVariant2.evidenceMatches.size).isEqualTo(1)
        assertThat(matchesVariant2.evidenceMatches).contains(hotspot2)

        val otherVariantGene1 =
            VARIANT_CRITERIA.copy(gene = "gene 1", chromosome = "X", position = 2, ref = "A", alt = "T", isReportable = true)
        assertThat(variantEvidence.findMatches(otherVariantGene1).evidenceMatches).isEmpty()
    }

    @Test
    fun `Should determine evidence for codons`() {
        assertEvidenceDeterminedForRange(actionableCodon)
    }

    @Test
    fun `Should determine evidence for exons`() {
        assertEvidenceDeterminedForRange(actionableExon)
    }

    @Test
    fun `Should determine evidence for genes`() {
        val gene1 = TestServeEvidenceFactory.createEvidenceForGene(GeneEvent.ANY_MUTATION, "gene 1")
        val gene2 = TestServeEvidenceFactory.createEvidenceForGene(GeneEvent.ACTIVATION, "gene 2")
        val gene3 = TestServeEvidenceFactory.createEvidenceForGene(GeneEvent.AMPLIFICATION, "gene 2")

        val variantEvidence = VariantEvidence.create(evidences = listOf(gene1, gene2, gene3), trials = emptyList())

        val variantGene1 = VARIANT_CRITERIA.copy(
            gene = "gene 1",
            codingEffect = CodingEffect.MISSENSE,
            isReportable = true
        )
        val matchesVariant1 = variantEvidence.findMatches(variantGene1)
        assertThat(matchesVariant1.evidenceMatches.size).isEqualTo(1)
        assertThat(matchesVariant1.evidenceMatches).contains(gene1)

        val variantGene2 = VARIANT_CRITERIA.copy(
            gene = "gene 2",
            codingEffect = CodingEffect.MISSENSE,
            isReportable = true
        )
        val matchesVariant2 = variantEvidence.findMatches(variantGene2)
        assertThat(matchesVariant2.evidenceMatches.size).isEqualTo(1)
        assertThat(matchesVariant2.evidenceMatches).contains(gene2)
    }

    private fun assertEvidenceDeterminedForRange(evidence: EfficacyEvidence) {
        val variantEvidence = VariantEvidence.create(evidences = listOf(evidence), trials = emptyList())

        val variantGene1 = VARIANT_CRITERIA.copy(
            gene = "gene 1",
            chromosome = "X",
            position = 6,
            isReportable = true,
            codingEffect = CodingEffect.MISSENSE
        )
        val matchesVariant1 = variantEvidence.findMatches(variantGene1)
        assertThat(matchesVariant1.evidenceMatches.size).isEqualTo(1)
        assertThat(matchesVariant1.evidenceMatches).contains(evidence)

        val otherVariantGene1 = VARIANT_CRITERIA.copy(
            gene = "gene 1",
            chromosome = "X",
            position = 2,
            isReportable = true,
            codingEffect = CodingEffect.MISSENSE
        )
        assertThat(variantEvidence.findMatches(otherVariantGene1).evidenceMatches).isEmpty()
    }
}