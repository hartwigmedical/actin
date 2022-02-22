package com.hartwig.actin.molecular.orange.filter;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.hartwig.actin.molecular.orange.datamodel.ImmutableTreatmentEvidence;
import com.hartwig.actin.molecular.orange.datamodel.TestTreatmentEvidenceFactory;

import org.jetbrains.annotations.NotNull;
import org.junit.Test;

public class ApplicabilityFilterTest {

    @Test
    public void canFilterOnGenes() {
        String nonApplicableGene = ApplicabilityFilter.NON_APPLICABLE_GENES.iterator().next();

        assertFalse(ApplicabilityFilter.isPotentiallyApplicable(testBuilder().gene(nonApplicableGene).build()));
        assertTrue(ApplicabilityFilter.isPotentiallyApplicable(testBuilder().gene("any gene").build()));
    }

    @Test
    public void canFilterOnEvents() {
        String nonApplicableEvent = ApplicabilityFilter.NON_APPLICABLE_EVENTS.iterator().next();

        assertFalse(ApplicabilityFilter.isPotentiallyApplicable(testBuilder().event(nonApplicableEvent).build()));
        assertTrue(ApplicabilityFilter.isPotentiallyApplicable(testBuilder().event("any event").build()));
    }

    @Test
    public void canFilterOnKeys() {
        ApplicabilityFilterKey nonApplicableKey = ApplicabilityFilter.NON_APPLICABLE_KEYS.iterator().next();

        assertFalse(ApplicabilityFilter.isPotentiallyApplicable(testBuilder().gene(nonApplicableKey.gene())
                .level(nonApplicableKey.level())
                .treatment(nonApplicableKey.treatment())
                .build()));
    }

    @NotNull
    private static ImmutableTreatmentEvidence.Builder testBuilder() {
        return ImmutableTreatmentEvidence.builder().from(TestTreatmentEvidenceFactory.create());
    }
}