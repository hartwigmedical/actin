package com.hartwig.actin.molecular.evidence.actionability

import com.hartwig.actin.datamodel.molecular.CodingEffect
import com.hartwig.actin.molecular.evidence.TestServeActionabilityFactory
import com.hartwig.actin.molecular.evidence.matching.VARIANT_CRITERIA
import com.hartwig.serve.datamodel.efficacy.EfficacyEvidence
import com.hartwig.serve.datamodel.molecular.MutationType
import com.hartwig.serve.datamodel.molecular.gene.GeneEvent
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class VariantEvidenceTest {

    private val actionableExon: EfficacyEvidence = TestServeActionabilityFactory.createEvidence(
        TestServeActionabilityFactory.createExon(
            "gene 1",
            "X",
            4,
            8,
            MutationType.ANY
        )
    )
    private val actionableCodon: EfficacyEvidence = TestServeActionabilityFactory.createEvidence(
        TestServeActionabilityFactory.createCodon(
            "gene 1",
            "X",
            4,
            8,
            MutationType.ANY
        )
    )

    @Test
    fun `Should determine evidence for hotpots`() {
        val hotspot1: EfficacyEvidence = TestServeActionabilityFactory.createEvidenceForHotspot("gene 1", "X", 2, "A", "G")
        val hotspot2: EfficacyEvidence = TestServeActionabilityFactory.createEvidenceForHotspot("gene 2", "X", 2, "A", "G")
        val hotspot3: EfficacyEvidence = TestServeActionabilityFactory.createEvidenceForHotspot("gene 1", "X", 2, "A", "C")
        val actionable = ActionableEvents(listOf(hotspot1, hotspot2, hotspot3), emptyList())
        val variantEvidence: VariantEvidence = VariantEvidence.create(actionable)

        val variantGene1 = VARIANT_CRITERIA.copy(gene = "gene 1", chromosome = "X", position = 2, ref = "A", alt = "G", isReportable = true)
        val matchesVariant1 = variantEvidence.findMatches(variantGene1)
        assertThat(matchesVariant1.evidences.size).isEqualTo(1)
        assertThat(matchesVariant1.evidences).contains(hotspot1)

        val variantGene2 = VARIANT_CRITERIA.copy(gene = "gene 2", chromosome = "X", position = 2, ref = "A", alt = "G", isReportable = true)
        val matchesVariant2 = variantEvidence.findMatches(variantGene2)
        assertThat(matchesVariant2.evidences.size).isEqualTo(1)
        assertThat(matchesVariant2.evidences).contains(hotspot2)

        val otherVariantGene1 =
            VARIANT_CRITERIA.copy(gene = "gene 1", chromosome = "X", position = 2, ref = "A", alt = "T", isReportable = true)
        assertThat(variantEvidence.findMatches(otherVariantGene1).evidences).isEmpty()
    }

    @Test
    fun `Should determine evidence for codons`() {
        assertEvidenceDeterminedForRange(ActionableEvents(listOf(actionableCodon), emptyList()), actionableCodon)
    }

    @Test
    fun `Should determine evidence for exons`() {
        assertEvidenceDeterminedForRange(ActionableEvents(listOf(actionableExon), emptyList()), actionableExon)
    }

    @Test
    fun `Should determine evidence for genes`() {
        val gene1: EfficacyEvidence = TestServeActionabilityFactory.createEvidenceForGene(GeneEvent.ANY_MUTATION, "gene 1")
        val gene2: EfficacyEvidence = TestServeActionabilityFactory.createEvidenceForGene(GeneEvent.ACTIVATION, "gene 2")
        val gene3: EfficacyEvidence = TestServeActionabilityFactory.createEvidenceForGene(GeneEvent.AMPLIFICATION, "gene 2")
        val actionable = ActionableEvents(listOf(gene1, gene2, gene3), emptyList())
        val variantEvidence: VariantEvidence = VariantEvidence.create(actionable)

        val variantGene1 = VARIANT_CRITERIA.copy(
            gene = "gene 1",
            codingEffect = CodingEffect.MISSENSE,
            isReportable = true
        )
        val matchesVariant1 = variantEvidence.findMatches(variantGene1)
        assertThat(matchesVariant1.evidences.size).isEqualTo(1)
        assertThat(matchesVariant1.evidences).contains(gene1)

        val variantGene2 = VARIANT_CRITERIA.copy(
            gene = "gene 2",
            codingEffect = CodingEffect.MISSENSE,
            isReportable = true
        )
        val matchesVariant2 = variantEvidence.findMatches(variantGene2)
        assertThat(matchesVariant2.evidences.size).isEqualTo(1)
        assertThat(matchesVariant2.evidences).contains(gene2)
    }

    private fun assertEvidenceDeterminedForRange(actionable: ActionableEvents, actionableRange: EfficacyEvidence) {
        val variantEvidence: VariantEvidence = VariantEvidence.create(actionable)

        val variantGene1 = VARIANT_CRITERIA.copy(
            gene = "gene 1",
            chromosome = "X",
            position = 6,
            isReportable = true,
            codingEffect = CodingEffect.MISSENSE
        )
        val matchesVariant1 = variantEvidence.findMatches(variantGene1)
        assertThat(matchesVariant1.evidences.size).isEqualTo(1)
        assertThat(matchesVariant1.evidences).contains(actionableRange)

        val otherVariantGene1 = VARIANT_CRITERIA.copy(
            gene = "gene 1",
            chromosome = "X",
            position = 2,
            isReportable = true,
            codingEffect = CodingEffect.MISSENSE
        )
        assertThat(variantEvidence.findMatches(otherVariantGene1).evidences).isEmpty()
    }
}