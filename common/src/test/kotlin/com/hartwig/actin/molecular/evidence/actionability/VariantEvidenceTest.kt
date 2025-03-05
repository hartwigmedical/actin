package com.hartwig.actin.molecular.evidence.actionability

import com.hartwig.actin.datamodel.molecular.driver.CodingEffect
import com.hartwig.actin.datamodel.molecular.driver.DriverLikelihood
import com.hartwig.actin.datamodel.molecular.driver.VariantType
import com.hartwig.actin.molecular.evidence.TestServeEvidenceFactory
import com.hartwig.actin.molecular.evidence.TestServeMolecularFactory
import com.hartwig.actin.molecular.evidence.TestServeTrialFactory
import com.hartwig.actin.molecular.evidence.matching.VariantMatchCriteria
import com.hartwig.serve.datamodel.molecular.MutationType
import com.hartwig.serve.datamodel.molecular.gene.GeneEvent
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

private val EVIDENCE_FOR_HOTSPOT =
    TestServeEvidenceFactory.createEvidenceForHotspot(
        variants = setOf(
            TestServeMolecularFactory.createVariantAnnotation(
                gene = "gene 1",
                chromosome = "1",
                position = 5,
                ref = "A",
                alt = "T"
            )
        )
    )
private val EVIDENCE_FOR_CODON = TestServeEvidenceFactory.createEvidenceForCodon(
    gene = "gene 1",
    chromosome = "1",
    start = 4,
    end = 6,
    applicableMutationType = MutationType.MISSENSE
)
private val EVIDENCE_FOR_EXON = TestServeEvidenceFactory.createEvidenceForExon(
    gene = "gene 1",
    chromosome = "1",
    start = 1,
    end = 10,
    applicableMutationType = MutationType.MISSENSE
)
private val ACT_EVIDENCE_FOR_GENE = TestServeEvidenceFactory.createEvidenceForGene(gene = "gene 1", geneEvent = GeneEvent.ACTIVATION)
private val ANY_EVIDENCE_FOR_GENE = TestServeEvidenceFactory.createEvidenceForGene(gene = "gene 1", geneEvent = GeneEvent.ANY_MUTATION)
private val AMP_EVIDENCE_FOR_GENE = TestServeEvidenceFactory.createEvidenceForGene(gene = "gene 1", geneEvent = GeneEvent.AMPLIFICATION)
private val OTHER_EVIDENCE = TestServeEvidenceFactory.createEvidenceForHla()

private val TRIAL_FOR_HOTSPOT =
    TestServeTrialFactory.createTrialForHotspot(
        variants = setOf(
            TestServeMolecularFactory.createVariantAnnotation(
                gene = "gene 1", chromosome = "1", position = 5, ref = "A", alt = "T"
            )
        )
    )
private val TRIAL_FOR_CODON = TestServeTrialFactory.createTrialForCodon(
    gene = "gene 1",
    chromosome = "1",
    start = 4,
    end = 6,
    applicableMutationType = MutationType.MISSENSE
)
private val TRIAL_FOR_EXON = TestServeTrialFactory.createTrialForExon(
    gene = "gene 1",
    chromosome = "1",
    start = 1,
    end = 10,
    applicableMutationType = MutationType.MISSENSE
)
private val ACT_TRIAL_FOR_GENE = TestServeTrialFactory.createTrialForGene(gene = "gene 1", geneEvent = GeneEvent.ACTIVATION)
private val ANY_TRIAL_FOR_GENE = TestServeTrialFactory.createTrialForGene(gene = "gene 1", geneEvent = GeneEvent.ANY_MUTATION)
private val AMP_TRIAL_FOR_GENE = TestServeTrialFactory.createTrialForGene(gene = "gene 1", geneEvent = GeneEvent.AMPLIFICATION)
private val OTHER_TRIAL = TestServeTrialFactory.createTrialForHla()

class VariantEvidenceTest {

    private val variantEvidence = VariantEvidence.create(
        evidences = listOf(
            EVIDENCE_FOR_HOTSPOT,
            EVIDENCE_FOR_CODON,
            EVIDENCE_FOR_EXON,
            ACT_EVIDENCE_FOR_GENE,
            ANY_EVIDENCE_FOR_GENE,
            AMP_EVIDENCE_FOR_GENE,
            OTHER_EVIDENCE
        ),
        trials = listOf(
            TRIAL_FOR_HOTSPOT,
            TRIAL_FOR_CODON,
            TRIAL_FOR_EXON,
            ACT_TRIAL_FOR_GENE,
            ANY_TRIAL_FOR_GENE,
            AMP_TRIAL_FOR_GENE,
            OTHER_TRIAL
        )
    )

    private val matchingVariant = VariantMatchCriteria(
        gene = "gene 1",
        codingEffect = CodingEffect.MISSENSE,
        type = VariantType.SNV,
        chromosome = "1",
        position = 5,
        ref = "A",
        alt = "T",
        driverLikelihood = DriverLikelihood.HIGH,
        isReportable = true
    )

    @Test
    fun `Should determine evidence and trials for exact matching hotpot`() {
        val matches = variantEvidence.findMatches(matchingVariant)
        assertThat(matches.evidenceMatches).containsExactlyInAnyOrder(
            EVIDENCE_FOR_HOTSPOT,
            EVIDENCE_FOR_CODON,
            EVIDENCE_FOR_EXON,
            ACT_EVIDENCE_FOR_GENE,
            ANY_EVIDENCE_FOR_GENE
        )

        assertThat(matches.matchingCriteriaPerTrialMatch).isEqualTo(
            mapOf(
                TRIAL_FOR_HOTSPOT to TRIAL_FOR_HOTSPOT.anyMolecularCriteria(),
                TRIAL_FOR_CODON to TRIAL_FOR_CODON.anyMolecularCriteria(),
                TRIAL_FOR_EXON to TRIAL_FOR_EXON.anyMolecularCriteria(),
                ACT_TRIAL_FOR_GENE to ACT_TRIAL_FOR_GENE.anyMolecularCriteria(),
                ANY_TRIAL_FOR_GENE to ANY_TRIAL_FOR_GENE.anyMolecularCriteria()
            )
        )
    }

    @Test
    fun `Should skip codon and exon evidence on non-matching type`() {
        val nonMatchingType = matchingVariant.copy(codingEffect = CodingEffect.NONSENSE_OR_FRAMESHIFT)

        val matches = variantEvidence.findMatches(nonMatchingType)
        assertThat(matches.evidenceMatches).containsExactlyInAnyOrder(
            EVIDENCE_FOR_HOTSPOT,
            ACT_EVIDENCE_FOR_GENE,
            ANY_EVIDENCE_FOR_GENE
        )

        assertThat(matches.matchingCriteriaPerTrialMatch).isEqualTo(
            mapOf(
                TRIAL_FOR_HOTSPOT to TRIAL_FOR_HOTSPOT.anyMolecularCriteria(),
                ACT_TRIAL_FOR_GENE to ACT_TRIAL_FOR_GENE.anyMolecularCriteria(),
                ANY_TRIAL_FOR_GENE to ANY_TRIAL_FOR_GENE.anyMolecularCriteria()
            )
        )
    }

    @Test
    fun `Should find no evidence for non-reportable variants`() {
        val nonReportable = matchingVariant.copy(isReportable = false)

        val matches = variantEvidence.findMatches(nonReportable)
        assertThat(matches.evidenceMatches).isEmpty()
        assertThat(matches.matchingCriteriaPerTrialMatch).isEmpty()
    }

    @Test
    fun `Should find no evidence for non high driver likelihood variants`() {
        val lowDriver = matchingVariant.copy(driverLikelihood = DriverLikelihood.LOW)

        val matches = variantEvidence.findMatches(lowDriver)
        assertThat(matches.evidenceMatches).isEmpty()
        assertThat(matches.matchingCriteriaPerTrialMatch).isEmpty()
    }
}