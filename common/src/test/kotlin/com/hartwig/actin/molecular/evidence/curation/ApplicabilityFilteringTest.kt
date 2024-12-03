package com.hartwig.actin.molecular.evidence.curation

import com.hartwig.actin.molecular.evidence.TestServeEvidenceFactory
import com.hartwig.serve.datamodel.molecular.gene.GeneEvent
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class ApplicabilityFilteringTest {

    @Test
    fun `Should filter hotspots on non-applicable gene`() {
        val nonApplicableGene = TestApplicabilityFilteringUtil.nonApplicableGene()
        assertThat(
            ApplicabilityFiltering.isApplicable(
                TestServeEvidenceFactory.createEvidenceForHotspot(nonApplicableGene).molecularCriterium().hotspots().first()
            )
        ).isFalse()
        assertThat(
            ApplicabilityFiltering.isApplicable(
                TestServeEvidenceFactory.createEvidenceForHotspot("other").molecularCriterium().hotspots().first()
            )
        ).isTrue()
    }

    @Test
    fun `Should filter ranges on non-applicable gene`() {
        val nonApplicableGene = TestApplicabilityFilteringUtil.nonApplicableGene()
        assertThat(
            ApplicabilityFiltering.isApplicable(
                TestServeEvidenceFactory.createEvidenceForCodon(nonApplicableGene).molecularCriterium().codons().first()
            )
        ).isFalse()
        assertThat(
            ApplicabilityFiltering.isApplicable(
                TestServeEvidenceFactory.createEvidenceForCodon("other").molecularCriterium().codons().first()
            )
        ).isTrue()
    }

    @Test
    fun `Should filter non-applicable genes`() {
        val nonApplicableGene = TestApplicabilityFilteringUtil.nonApplicableGene()
        assertThat(
            ApplicabilityFiltering.isApplicable(
                TestServeEvidenceFactory.createEvidenceForGene(gene = nonApplicableGene).molecularCriterium().genes().first()
            )
        ).isFalse()
        assertThat(
            ApplicabilityFiltering.isApplicable(
                TestServeEvidenceFactory.createEvidenceForGene(GeneEvent.ANY_MUTATION, "other").molecularCriterium().genes()
                    .first()
            )
        ).isTrue()

        val nonApplicableAmp = TestApplicabilityFilteringUtil.nonApplicableAmplification()
        assertThat(
            ApplicabilityFiltering.isApplicable(
                TestServeEvidenceFactory.createEvidenceForGene(GeneEvent.ANY_MUTATION, nonApplicableAmp).molecularCriterium()
                    .genes().first()
            )
        ).isTrue()

        assertThat(
            ApplicabilityFiltering.isApplicable(
                TestServeEvidenceFactory.createEvidenceForGene(GeneEvent.AMPLIFICATION, "other gene").molecularCriterium()
                    .genes().first()
            )
        ).isTrue()

        assertThat(
            ApplicabilityFiltering.isApplicable(
                TestServeEvidenceFactory.createEvidenceForGene(GeneEvent.AMPLIFICATION, nonApplicableAmp).molecularCriterium()
                    .genes().first()
            )
        ).isFalse()
    }
}