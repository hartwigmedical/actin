package com.hartwig.actin.molecular.evidence.curation

import com.hartwig.actin.molecular.evidence.TestServeActionabilityFactory
import com.hartwig.serve.datamodel.molecular.gene.GeneEvent
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class ApplicabilityFilteringTest {

    @Test
    fun `Should filter hotspots on non-applicable gene`() {
        val nonApplicableGene = TestApplicabilityFilteringUtil.nonApplicableGene()
        assertThat(
            ApplicabilityFiltering.isApplicable(
                TestServeActionabilityFactory.createEvidenceForHotspot(nonApplicableGene).molecularCriterium().hotspots().first()
            )
        ).isFalse()
        assertThat(
            ApplicabilityFiltering.isApplicable(
                TestServeActionabilityFactory.createEvidenceForHotspot("other").molecularCriterium().hotspots().first()
            )
        ).isTrue()
    }

    @Test
    fun `Should filter ranges on non-applicable gene`() {
        val nonApplicableGene = TestApplicabilityFilteringUtil.nonApplicableGene()
        assertThat(
            ApplicabilityFiltering.isApplicable(
                TestServeActionabilityFactory.createEvidenceForCodon(nonApplicableGene).molecularCriterium().codons().first()
            )
        ).isFalse()
        assertThat(
            ApplicabilityFiltering.isApplicable(
                TestServeActionabilityFactory.createEvidenceForCodon("other").molecularCriterium().codons().first()
            )
        ).isTrue()
    }

    @Test
    fun `Should filter non-applicable genes`() {
        val nonApplicableGene = TestApplicabilityFilteringUtil.nonApplicableGene()
        assertThat(
            ApplicabilityFiltering.isApplicable(
                TestServeActionabilityFactory.createEvidenceForGene(gene = nonApplicableGene).molecularCriterium().genes().first()
            )
        ).isFalse()
        assertThat(
            ApplicabilityFiltering.isApplicable(
                TestServeActionabilityFactory.createEvidenceForGene(GeneEvent.ANY_MUTATION, "other").molecularCriterium().genes()
                    .first()
            )
        ).isTrue()

        val nonApplicableAmp = TestApplicabilityFilteringUtil.nonApplicableAmplification()
        assertThat(
            ApplicabilityFiltering.isApplicable(
                TestServeActionabilityFactory.createEvidenceForGene(GeneEvent.ANY_MUTATION, nonApplicableAmp).molecularCriterium()
                    .genes().first()
            )
        ).isTrue()

        assertThat(
            ApplicabilityFiltering.isApplicable(
                TestServeActionabilityFactory.createEvidenceForGene(GeneEvent.AMPLIFICATION, "other gene").molecularCriterium()
                    .genes().first()
            )
        ).isTrue()

        assertThat(
            ApplicabilityFiltering.isApplicable(
                TestServeActionabilityFactory.createEvidenceForGene(GeneEvent.AMPLIFICATION, nonApplicableAmp).molecularCriterium()
                    .genes().first()
            )
        ).isFalse()
    }
}