package com.hartwig.actin.report.interpretation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.Sets;
import com.hartwig.actin.molecular.datamodel.ImmutableMolecularEvidence;
import com.hartwig.actin.molecular.datamodel.ImmutableMolecularRecord;
import com.hartwig.actin.molecular.datamodel.MolecularEvidence;
import com.hartwig.actin.molecular.datamodel.MolecularRecord;
import com.hartwig.actin.molecular.datamodel.TestMolecularDataFactory;

import org.jetbrains.annotations.NotNull;
import org.junit.Test;

public class EvidenceInterpreterTest {

    @Test
    public void canInterpretEvidence() {
        MolecularRecord molecular = createTestMolecularRecord();

        assertEquals(Sets.newHashSet("event 1"), EvidenceInterpreter.eventsWithApprovedEvidence(molecular));
        assertEquals(Sets.newHashSet("event 2"), EvidenceInterpreter.eventsWithActinEvidence(molecular));
        assertEquals(Sets.newHashSet("event 3"), EvidenceInterpreter.additionalEventsWithExternalTrialEvidence(molecular));
        assertEquals(Sets.newHashSet(create("event 3", "trial 3")), EvidenceInterpreter.additionalEvidenceForExternalTrials(molecular));
        assertTrue(EvidenceInterpreter.additionalEventsWithExperimentalEvidence(molecular).isEmpty());
        assertEquals(Sets.newHashSet("event 3"), EvidenceInterpreter.additionalEventsWithOtherEvidence(molecular));
    }

    @NotNull
    private static MolecularRecord createTestMolecularRecord() {
        return ImmutableMolecularRecord.builder()
                .from(TestMolecularDataFactory.createMinimalTestMolecularRecord())
                .approvedResponsiveEvidence(Sets.newHashSet(create("event 1", "treatment 1")))
                .actinTrials(Sets.newHashSet(create("event 2", "trial 1")))
                .externalTrials(Sets.newHashSet(create("event 1", "trial 1"), create("event 2", "trial 1"), create("event 3", "trial 3")))
                .experimentalResponsiveEvidence(Sets.newHashSet(create("event 2", "trial 1")))
                .otherResponsiveEvidence(Sets.newHashSet(create("event 1", "treatment 1"), create("event 3", "treatment 3")))
                .build();
    }

    @NotNull
    private static MolecularEvidence create(@NotNull String event, @NotNull String treatment) {
        return ImmutableMolecularEvidence.builder().event(event).treatment(treatment).build();
    }
}