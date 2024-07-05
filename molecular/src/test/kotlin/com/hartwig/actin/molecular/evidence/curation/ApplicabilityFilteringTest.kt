package com.hartwig.actin.molecular.evidence.curation

import com.hartwig.actin.molecular.evidence.actionability.TestServeActionabilityFactory
import com.hartwig.serve.datamodel.gene.GeneEvent
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class ApplicabilityFilteringTest {

    @Test
    fun `Should filter hotspots on non-applicable gene`() {
        val nonApplicableGene = TestApplicabilityFilteringUtil.nonApplicableGene()
        assertThat(
            ApplicabilityFiltering.isApplicable(
                TestServeActionabilityFactory.hotspotBuilder().gene(nonApplicableGene).build()
            )
        ).isFalse()
        assertThat(ApplicabilityFiltering.isApplicable(TestServeActionabilityFactory.hotspotBuilder().gene("other").build())).isTrue()
    }

    @Test
    fun `Should filter ranges on non-applicable gene`() {
        val nonApplicableGene = TestApplicabilityFilteringUtil.nonApplicableGene()
        assertThat(
            ApplicabilityFiltering.isApplicable(
                TestServeActionabilityFactory.rangeBuilder().gene(nonApplicableGene).build()
            )
        ).isFalse()
        assertThat(ApplicabilityFiltering.isApplicable(TestServeActionabilityFactory.rangeBuilder().gene("other").build())).isTrue()
    }

    @Test
    fun `Should filter non-applicable genes`() {
        val nonApplicableGene = TestApplicabilityFilteringUtil.nonApplicableGene()
        assertThat(
            ApplicabilityFiltering.isApplicable(
                TestServeActionabilityFactory.geneBuilder().gene(nonApplicableGene).build()
            )
        ).isFalse()
        assertThat(ApplicabilityFiltering.isApplicable(TestServeActionabilityFactory.geneBuilder().gene("other").build())).isTrue()

        val nonApplicableAmp = TestApplicabilityFilteringUtil.nonApplicableAmplification()
        assertThat(
            ApplicabilityFiltering.isApplicable(
                TestServeActionabilityFactory.geneBuilder().gene(nonApplicableAmp).event(GeneEvent.ANY_MUTATION).build()
            )
        ).isTrue()

        assertThat(
            ApplicabilityFiltering.isApplicable(
                TestServeActionabilityFactory.geneBuilder().gene("other gene").event(GeneEvent.AMPLIFICATION).build()
            )
        ).isTrue()

        assertThat(
            ApplicabilityFiltering.isApplicable(
                TestServeActionabilityFactory.geneBuilder().gene(nonApplicableAmp).event(GeneEvent.AMPLIFICATION).build()
            )
        ).isFalse()
    }
}