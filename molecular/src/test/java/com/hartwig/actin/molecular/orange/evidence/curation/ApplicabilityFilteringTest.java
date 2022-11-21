package com.hartwig.actin.molecular.orange.evidence.curation;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.hartwig.actin.molecular.orange.evidence.actionability.TestActionabilityFactory;
import com.hartwig.serve.datamodel.gene.GeneEvent;

import org.junit.Test;

public class ApplicabilityFilteringTest {

    @Test
    public void canFilterHotspots() {
        String nonApplicableGene = ApplicabilityFiltering.NON_APPLICABLE_GENES.iterator().next();

        assertFalse(ApplicabilityFiltering.isApplicable(TestActionabilityFactory.actionableHotspotBuilder().gene(nonApplicableGene).build()));
        assertTrue(ApplicabilityFiltering.isApplicable(TestActionabilityFactory.actionableHotspotBuilder().gene("other").build()));
    }

    @Test
    public void canFilterRanges() {
        String nonApplicableGene = ApplicabilityFiltering.NON_APPLICABLE_GENES.iterator().next();

        assertFalse(ApplicabilityFiltering.isApplicable(TestActionabilityFactory.actionableRangeBuilder().gene(nonApplicableGene).build()));
        assertTrue(ApplicabilityFiltering.isApplicable(TestActionabilityFactory.actionableRangeBuilder().gene("other").build()));
    }

    @Test
    public void canFilterGenes() {
        String nonApplicableGene = ApplicabilityFiltering.NON_APPLICABLE_GENES.iterator().next();

        assertFalse(ApplicabilityFiltering.isApplicable(TestActionabilityFactory.actionableGeneBuilder().gene(nonApplicableGene).build()));
        assertTrue(ApplicabilityFiltering.isApplicable(TestActionabilityFactory.actionableGeneBuilder().gene("other").build()));

        String nonApplicableAmp = ApplicabilityFiltering.NON_APPLICABLE_AMPLIFICATIONS.iterator().next();
        assertTrue(ApplicabilityFiltering.isApplicable(TestActionabilityFactory.actionableGeneBuilder()
                .gene(nonApplicableAmp)
                .event(GeneEvent.ANY_MUTATION)
                .build()));

        assertFalse(ApplicabilityFiltering.isApplicable(TestActionabilityFactory.actionableGeneBuilder()
                .gene(nonApplicableAmp)
                .event(GeneEvent.AMPLIFICATION)
                .build()));
    }
}