package com.hartwig.actin.molecular.evidence.curation

import com.hartwig.actin.molecular.evidence.actionability.TestServeActionabilityFactory
import com.hartwig.serve.datamodel.gene.GeneEvent
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ApplicabilityFilteringTest {

    @Test
    fun `Should filter hotspots on non-applicable gene`() {
        val nonApplicableGene = TestApplicabilityFilteringUtil.nonApplicableGene()
        assertFalse(
            ApplicabilityFiltering.isApplicable(
                TestServeActionabilityFactory.hotspotBuilder().gene(nonApplicableGene).build()
            )
        )
        assertTrue(ApplicabilityFiltering.isApplicable(TestServeActionabilityFactory.hotspotBuilder().gene("other").build()))
    }

    @Test
    fun `Should filter ranges on non-applicable gene`() {
        val nonApplicableGene = TestApplicabilityFilteringUtil.nonApplicableGene()
        assertFalse(
            ApplicabilityFiltering.isApplicable(
                TestServeActionabilityFactory.rangeBuilder().gene(nonApplicableGene).build()
            )
        )
        assertTrue(ApplicabilityFiltering.isApplicable(TestServeActionabilityFactory.rangeBuilder().gene("other").build()))
    }

    @Test
    fun `Should filter non-applicable genes`() {
        val nonApplicableGene = TestApplicabilityFilteringUtil.nonApplicableGene()
        assertFalse(ApplicabilityFiltering.isApplicable(TestServeActionabilityFactory.geneBuilder().gene(nonApplicableGene).build()))
        assertTrue(ApplicabilityFiltering.isApplicable(TestServeActionabilityFactory.geneBuilder().gene("other").build()))

        val nonApplicableAmp = TestApplicabilityFilteringUtil.nonApplicableAmplification()
        assertTrue(
            ApplicabilityFiltering.isApplicable(
                TestServeActionabilityFactory.geneBuilder()
                    .gene(nonApplicableAmp)
                    .event(GeneEvent.ANY_MUTATION)
                    .build()
            )
        )

        assertTrue(
            ApplicabilityFiltering.isApplicable(
                TestServeActionabilityFactory.geneBuilder()
                    .gene("other gene")
                    .event(GeneEvent.AMPLIFICATION)
                    .build()
            )
        )

        assertFalse(
            ApplicabilityFiltering.isApplicable(
                TestServeActionabilityFactory.geneBuilder()
                    .gene(nonApplicableAmp)
                    .event(GeneEvent.AMPLIFICATION)
                    .build()
            )
        )
    }
}