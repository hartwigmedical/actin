package com.hartwig.actin.molecular.orange.evidence;

import static org.junit.Assert.assertNotNull;

import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;
import com.hartwig.actin.doid.datamodel.DoidEntry;
import com.hartwig.actin.doid.datamodel.TestDoidEntryFactory;
import com.hartwig.actin.molecular.orange.evidence.curation.ExternalTrialMapping;
import com.hartwig.actin.molecular.serve.KnownGene;
import com.hartwig.serve.datamodel.ActionableEvents;
import com.hartwig.serve.datamodel.ImmutableActionableEvents;
import com.hartwig.serve.datamodel.ImmutableKnownEvents;
import com.hartwig.serve.datamodel.KnownEvents;

import org.junit.Test;

public class EvidenceDatabaseFactoryTest {

    @Test
    public void canCreateFromMinimalInputs() {
        KnownEvents knownEvents = ImmutableKnownEvents.builder().build();
        List<KnownGene> knownGenes = Lists.newArrayList();
        ActionableEvents actionableEvents = ImmutableActionableEvents.builder().build();
        List<ExternalTrialMapping> externalTrialMappings = Lists.newArrayList();
        DoidEntry doidEntry = TestDoidEntryFactory.createMinimalTestDoidEntry();
        Set<String> tumorDoids = null;

        assertNotNull(EvidenceDatabaseFactory.create(knownEvents,
                knownGenes,
                actionableEvents,
                externalTrialMappings,
                doidEntry,
                tumorDoids));
    }
}