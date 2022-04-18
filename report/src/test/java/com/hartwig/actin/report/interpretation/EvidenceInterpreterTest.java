package com.hartwig.actin.report.interpretation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.Sets;
import com.hartwig.actin.molecular.datamodel.TestMolecularDataFactory;
import com.hartwig.actin.molecular.datamodel.evidence.EvidenceEntry;
import com.hartwig.actin.molecular.datamodel.evidence.ImmutableEvidenceEntry;
import com.hartwig.actin.molecular.datamodel.evidence.ImmutableMolecularEvidence;
import com.hartwig.actin.molecular.datamodel.evidence.MolecularEvidence;

import org.jetbrains.annotations.NotNull;
import org.junit.Test;

public class EvidenceInterpreterTest {

    @Test
    public void canInterpretEvidence() {
        MolecularEvidence evidence = createTestEvidence();

        assertEquals(Sets.newHashSet("event 1"), EvidenceInterpreter.eventsWithApprovedEvidence(evidence));
        assertEquals(Sets.newHashSet("event 2"), EvidenceInterpreter.eventsWithActinEvidence(evidence));
        assertEquals(Sets.newHashSet("event 3"), EvidenceInterpreter.additionalEventsWithExternalTrialEvidence(evidence));
        assertEquals(Sets.newHashSet(create("event 3", "trial 3")), EvidenceInterpreter.additionalEvidenceForExternalTrials(evidence));
        assertTrue(EvidenceInterpreter.additionalEventsWithOnLabelExperimentalEvidence(evidence).isEmpty());
        assertEquals(Sets.newHashSet("event 3"), EvidenceInterpreter.additionalEventsWithOffLabelExperimentalEvidence(evidence));
    }

    @NotNull
    private static MolecularEvidence createTestEvidence() {
        return ImmutableMolecularEvidence.builder()
                .from(TestMolecularDataFactory.createMinimalTestMolecularRecord().evidence())
                .approvedEvidence(Sets.newHashSet(create("event 1", "treatment 1")))
                .actinTrials(Sets.newHashSet(create("event 2", "trial 1")))
                .externalTrials(Sets.newHashSet(create("event 1", "trial 1"), create("event 2", "trial 1"), create("event 3", "trial 3")))
                .onLabelExperimentalEvidence(Sets.newHashSet(create("event 2", "trial 1")))
                .offLabelExperimentalEvidence(Sets.newHashSet(create("event 1", "treatment 1"), create("event 3", "treatment 3")))
                .build();
    }

    @NotNull
    private static EvidenceEntry create(@NotNull String event, @NotNull String treatment) {
        return ImmutableEvidenceEntry.builder().event(event).treatment(treatment).build();
    }
}