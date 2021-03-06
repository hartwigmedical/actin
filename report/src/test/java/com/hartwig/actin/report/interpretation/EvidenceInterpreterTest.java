package com.hartwig.actin.report.interpretation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.Sets;
import com.hartwig.actin.molecular.datamodel.TestMolecularFactory;
import com.hartwig.actin.molecular.datamodel.evidence.ActinTrialEvidence;
import com.hartwig.actin.molecular.datamodel.evidence.ActinTrialEvidenceTestFactory;
import com.hartwig.actin.molecular.datamodel.evidence.ExternalTrialEvidence;
import com.hartwig.actin.molecular.datamodel.evidence.ImmutableExternalTrialEvidence;
import com.hartwig.actin.molecular.datamodel.evidence.ImmutableMolecularEvidence;
import com.hartwig.actin.molecular.datamodel.evidence.ImmutableTreatmentEvidence;
import com.hartwig.actin.molecular.datamodel.evidence.MolecularEvidence;
import com.hartwig.actin.molecular.datamodel.evidence.TreatmentEvidence;

import org.jetbrains.annotations.NotNull;
import org.junit.Test;

public class EvidenceInterpreterTest {

    @Test
    public void canInterpretEvidence() {
        MolecularEvidence evidence = createTestEvidence();

        assertEquals(Sets.newHashSet("event 1"), EvidenceInterpreter.eventsWithApprovedEvidence(evidence));
        assertEquals(Sets.newHashSet("event 2"), EvidenceInterpreter.eventsWithInclusiveActinEvidence(evidence));
        assertEquals(Sets.newHashSet("event 3"), EvidenceInterpreter.additionalEventsWithExternalTrialEvidence(evidence));
        assertTrue(EvidenceInterpreter.additionalEventsWithOnLabelExperimentalEvidence(evidence).isEmpty());
        assertEquals(Sets.newHashSet("event 3"), EvidenceInterpreter.additionalEventsWithOffLabelExperimentalEvidence(evidence));
    }

    @NotNull
    private static MolecularEvidence createTestEvidence() {
        return ImmutableMolecularEvidence.builder()
                .from(TestMolecularFactory.createMinimalTestMolecularRecord().evidence())
                .actinTrials(Sets.newHashSet(createActinTrialEvidence("event 2", "trial 1", true),
                        createActinTrialEvidence("event 1", "trial 3", false)))
                .externalTrials(Sets.newHashSet(createExternalTrialEvidence("event 1", "trial 1"),
                        createExternalTrialEvidence("event 2", "trial 1"),
                        createExternalTrialEvidence("event 3", "trial 3")))
                .approvedEvidence(Sets.newHashSet(createTreatmentEvidence("event 1", "treatment 1")))
                .onLabelExperimentalEvidence(Sets.newHashSet(createTreatmentEvidence("event 2", "trial 1")))
                .offLabelExperimentalEvidence(Sets.newHashSet(createTreatmentEvidence("event 1", "treatment 1"),
                        createTreatmentEvidence("event 3", "treatment 3")))
                .build();
    }

    @NotNull
    private static ActinTrialEvidence createActinTrialEvidence(@NotNull String event, @NotNull String trial, boolean isInclusionCriterion) {
        return ActinTrialEvidenceTestFactory.builder().event(event).trialAcronym(trial).isInclusionCriterion(isInclusionCriterion).build();
    }

    @NotNull
    private static ExternalTrialEvidence createExternalTrialEvidence(@NotNull String event, @NotNull String trial) {
        return ImmutableExternalTrialEvidence.builder().event(event).trial(trial).build();
    }

    @NotNull
    private static TreatmentEvidence createTreatmentEvidence(@NotNull String event, @NotNull String treatment) {
        return ImmutableTreatmentEvidence.builder().event(event).treatment(treatment).build();
    }
}