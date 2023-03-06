package com.hartwig.actin.report.interpretation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Set;

import com.google.common.collect.Lists;
import com.hartwig.actin.molecular.interpretation.AggregatedEvidence;
import com.hartwig.actin.molecular.interpretation.ImmutableAggregatedEvidence;

import org.junit.Test;

public class EvidenceInterpreterTest {

    @Test
    public void canInterpretEvidence() {
        EvaluatedCohort trialWithInclusion = EvaluatedCohortTestFactory.builder().addMolecularEvents("inclusion").build();

        EvidenceInterpreter interpreter = EvidenceInterpreter.fromEvaluatedTrials(Lists.newArrayList(trialWithInclusion));

        AggregatedEvidence evidence = ImmutableAggregatedEvidence.builder()
                .putApprovedTreatmentsPerEvent("approved", "treatment")
                .putExternalEligibleTrialsPerEvent("external", "treatment")
                .putExternalEligibleTrialsPerEvent("approved", "treatment")
                .putExternalEligibleTrialsPerEvent("inclusion", "treatment")
                .putOnLabelExperimentalTreatmentsPerEvent("on-label", "treatment")
                .putOnLabelExperimentalTreatmentsPerEvent("approved", "treatment")
                .putOffLabelExperimentalTreatmentsPerEvent("off-label", "treatment")
                .putOffLabelExperimentalTreatmentsPerEvent("on-label", "treatment")
                .putPreClinicalTreatmentsPerEvent("pre-clinical", "treatment")
                .putKnownResistantTreatmentsPerEvent("known", "treatment")
                .putSuspectResistanceTreatmentsPerEvent("suspect", "treatment")
                .build();

        Set<String> approved = interpreter.eventsWithApprovedEvidence(evidence);
        assertEquals(1, approved.size());
        assertTrue(approved.contains("approved"));

        Set<String> external = interpreter.additionalEventsWithExternalTrialEvidence(evidence);
        assertEquals(1, external.size());
        assertTrue(external.contains("external"));

        Set<String> onLabel = interpreter.additionalEventsWithOnLabelExperimentalEvidence(evidence);
        assertEquals(1, onLabel.size());
        assertTrue(onLabel.contains("on-label"));

        Set<String> offLabel = interpreter.additionalEventsWithOffLabelExperimentalEvidence(evidence);
        assertEquals(1, offLabel.size());
        assertTrue(offLabel.contains("off-label"));
    }
}