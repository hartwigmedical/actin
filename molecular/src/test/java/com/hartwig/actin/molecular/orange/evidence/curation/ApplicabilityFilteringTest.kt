package com.hartwig.actin.molecular.orange.evidence.curation

import com.hartwig.actin.molecular.orange.evidence.actionability.TestServeActionabilityFactory
import com.hartwig.serve.datamodel.gene.GeneEvent
import org.junit.Assert
import org.junit.Test

class ApplicabilityFilteringTest {
    @Test
    fun canFilterHotspots() {
        val nonApplicableGene = TestApplicabilityFilteringUtil.nonApplicableGene()
        Assert.assertFalse(ApplicabilityFiltering.isApplicable(TestServeActionabilityFactory.hotspotBuilder().gene(nonApplicableGene).build()))
        Assert.assertTrue(ApplicabilityFiltering.isApplicable(TestServeActionabilityFactory.hotspotBuilder().gene("other").build()))
    }

    @Test
    fun canFilterRanges() {
        val nonApplicableGene = TestApplicabilityFilteringUtil.nonApplicableGene()
        Assert.assertFalse(ApplicabilityFiltering.isApplicable(TestServeActionabilityFactory.rangeBuilder().gene(nonApplicableGene).build()))
        Assert.assertTrue(ApplicabilityFiltering.isApplicable(TestServeActionabilityFactory.rangeBuilder().gene("other").build()))
    }

    @Test
    fun canFilterGenes() {
        val nonApplicableGene = TestApplicabilityFilteringUtil.nonApplicableGene()
        Assert.assertFalse(ApplicabilityFiltering.isApplicable(TestServeActionabilityFactory.geneBuilder().gene(nonApplicableGene).build()))
        Assert.assertTrue(ApplicabilityFiltering.isApplicable(TestServeActionabilityFactory.geneBuilder().gene("other").build()))
        val nonApplicableAmp = TestApplicabilityFilteringUtil.nonApplicableAmplification()
        Assert.assertTrue(ApplicabilityFiltering.isApplicable(TestServeActionabilityFactory.geneBuilder()
            .gene(nonApplicableAmp)
            .event(GeneEvent.ANY_MUTATION)
            .build()))
        Assert.assertTrue(ApplicabilityFiltering.isApplicable(TestServeActionabilityFactory.geneBuilder()
            .gene("other gene")
            .event(GeneEvent.AMPLIFICATION)
            .build()))
        Assert.assertFalse(ApplicabilityFiltering.isApplicable(TestServeActionabilityFactory.geneBuilder()
            .gene(nonApplicableAmp)
            .event(GeneEvent.AMPLIFICATION)
            .build()))
    }
}