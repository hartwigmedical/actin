package com.hartwig.actin.molecular.orange.interpretation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import com.hartwig.actin.molecular.datamodel.evidence.ActionableEvidence;
import com.hartwig.actin.molecular.datamodel.evidence.TestActionableEvidenceFactory;
import com.hartwig.actin.molecular.orange.evidence.actionability.ActionabilityConstants;
import com.hartwig.actin.molecular.orange.evidence.actionability.ActionabilityMatch;
import com.hartwig.actin.molecular.orange.evidence.actionability.ImmutableActionabilityMatch;
import com.hartwig.actin.molecular.orange.evidence.actionability.TestServeActionabilityFactory;
import com.hartwig.serve.datamodel.ActionableEvent;
import com.hartwig.serve.datamodel.EvidenceDirection;
import com.hartwig.serve.datamodel.EvidenceLevel;

import org.jetbrains.annotations.NotNull;
import org.junit.Test;

public class ActionableEvidenceFactoryTest {

    @Test
    public void handlesNoMatch() {
        assertNull(ActionableEvidenceFactory.create(null));
    }

    @Test
    public void canMapResponsiveEvidence() {
        ActionabilityMatch match = ImmutableActionabilityMatch.builder()
                .addOnLabelEvents(evidence("A on-label responsive", EvidenceLevel.A, EvidenceDirection.RESPONSIVE))
                .addOnLabelEvents(evidence("A on-label predicted responsive", EvidenceLevel.A, EvidenceDirection.PREDICTED_RESPONSIVE))
                .addOnLabelEvents(evidence("B on-label responsive", EvidenceLevel.B, EvidenceDirection.RESPONSIVE))
                .addOnLabelEvents(evidence("B on-label predicted responsive", EvidenceLevel.B, EvidenceDirection.PREDICTED_RESPONSIVE))
                .addOnLabelEvents(evidence("C on-label responsive", EvidenceLevel.C, EvidenceDirection.RESPONSIVE))
                .addOnLabelEvents(evidence("C on-label predicted responsive", EvidenceLevel.C, EvidenceDirection.PREDICTED_RESPONSIVE))
                .addOffLabelEvents(evidence("A off-label responsive", EvidenceLevel.A, EvidenceDirection.RESPONSIVE))
                .addOffLabelEvents(evidence("A off-label predicted responsive", EvidenceLevel.A, EvidenceDirection.PREDICTED_RESPONSIVE))
                .addOffLabelEvents(evidence("B off-label responsive", EvidenceLevel.B, EvidenceDirection.RESPONSIVE))
                .addOffLabelEvents(evidence("B off-label predicted responsive", EvidenceLevel.B, EvidenceDirection.PREDICTED_RESPONSIVE))
                .addOffLabelEvents(evidence("C off-label responsive", EvidenceLevel.C, EvidenceDirection.RESPONSIVE))
                .addOffLabelEvents(evidence("C off-label predicted responsive", EvidenceLevel.C, EvidenceDirection.PREDICTED_RESPONSIVE))
                .build();

        ActionableEvidence evidence = ActionableEvidenceFactory.create(match);
        assertEquals(1, evidence.approvedTreatments().size());
        assertTrue(evidence.approvedTreatments().contains("A on-label responsive"));

        assertTrue(evidence.externalEligibleTrials().isEmpty());

        assertEquals(4, evidence.onLabelExperimentalTreatments().size());
        assertTrue(evidence.onLabelExperimentalTreatments().contains("A on-label predicted responsive"));
        assertTrue(evidence.onLabelExperimentalTreatments().contains("B on-label responsive"));
        assertTrue(evidence.onLabelExperimentalTreatments().contains("A off-label responsive"));
        assertTrue(evidence.onLabelExperimentalTreatments().contains("A off-label predicted responsive"));

        assertEquals(1, evidence.offLabelExperimentalTreatments().size());
        assertTrue(evidence.offLabelExperimentalTreatments().contains("B off-label responsive"));

        assertEquals(6, evidence.preClinicalTreatments().size());
        assertTrue(evidence.preClinicalTreatments().contains("B on-label predicted responsive"));
        assertTrue(evidence.preClinicalTreatments().contains("C on-label responsive"));
        assertTrue(evidence.preClinicalTreatments().contains("C on-label predicted responsive"));
        assertTrue(evidence.preClinicalTreatments().contains("B off-label predicted responsive"));
        assertTrue(evidence.preClinicalTreatments().contains("C off-label responsive"));
        assertTrue(evidence.preClinicalTreatments().contains("C off-label predicted responsive"));

        assertTrue(evidence.knownResistantTreatments().isEmpty());
        assertTrue(evidence.suspectResistantTreatments().isEmpty());
    }

    @Test
    public void canMapResistanceEvidence() {
        ActionabilityMatch match = ImmutableActionabilityMatch.builder()
                .addOnLabelEvents(evidence("On-label responsive A", EvidenceLevel.A, EvidenceDirection.RESPONSIVE))
                .addOnLabelEvents(evidence("On-label responsive A", EvidenceLevel.A, EvidenceDirection.RESISTANT))
                .addOnLabelEvents(evidence("On-label responsive C", EvidenceLevel.A, EvidenceDirection.RESPONSIVE))
                .addOnLabelEvents(evidence("On-label responsive C", EvidenceLevel.C, EvidenceDirection.RESISTANT))
                .addOffLabelEvents(evidence("Off-label responsive", EvidenceLevel.B, EvidenceDirection.RESPONSIVE))
                .addOffLabelEvents(evidence("Off-label responsive", EvidenceLevel.A, EvidenceDirection.PREDICTED_RESISTANT))
                .addOffLabelEvents(evidence("Other off-label resistant", EvidenceLevel.A, EvidenceDirection.RESISTANT))
                .build();

        ActionableEvidence evidence = ActionableEvidenceFactory.create(match);

        assertEquals(1, evidence.knownResistantTreatments().size());
        assertTrue(evidence.knownResistantTreatments().contains("On-label responsive A"));

        assertEquals(2, evidence.suspectResistantTreatments().size());
        assertTrue(evidence.suspectResistantTreatments().contains("Off-label responsive"));
        assertTrue(evidence.suspectResistantTreatments().contains("On-label responsive C"));
    }

    @Test
    public void canMapTrials() {
        ActionabilityMatch match = ImmutableActionabilityMatch.builder()
                .addOnLabelEvents(trial("On-label responsive trial", EvidenceDirection.RESPONSIVE))
                .addOnLabelEvents(trial("On-label resistant trial", EvidenceDirection.RESISTANT))
                .addOffLabelEvents(trial("Off-label responsive trial", EvidenceDirection.RESPONSIVE))
                .addOffLabelEvents(trial("Off-label resistant trial", EvidenceDirection.RESISTANT))
                .build();

        ActionableEvidence evidence = ActionableEvidenceFactory.create(match);

        assertTrue(evidence.approvedTreatments().isEmpty());

        assertEquals(1, evidence.externalEligibleTrials().size());
        assertTrue(evidence.externalEligibleTrials().contains("On-label responsive trial"));

        assertTrue(evidence.onLabelExperimentalTreatments().isEmpty());
        assertTrue(evidence.offLabelExperimentalTreatments().isEmpty());
        assertTrue(evidence.preClinicalTreatments().isEmpty());
        assertTrue(evidence.knownResistantTreatments().isEmpty());
        assertTrue(evidence.suspectResistantTreatments().isEmpty());
    }

    @Test
    public void ignoresEvidenceWithNoBenefit() {
        ActionabilityMatch match = ImmutableActionabilityMatch.builder()
                .addOnLabelEvents(evidence("A on-label no-benefit", EvidenceLevel.A, EvidenceDirection.NO_BENEFIT))
                .addOffLabelEvents(evidence("A off-label no-benefit", EvidenceLevel.A, EvidenceDirection.NO_BENEFIT))
                .build();

        ActionableEvidence evidence = ActionableEvidenceFactory.create(match);
        assertTrue(evidence.approvedTreatments().isEmpty());
        assertTrue(evidence.externalEligibleTrials().isEmpty());
        assertTrue(evidence.onLabelExperimentalTreatments().isEmpty());
        assertTrue(evidence.offLabelExperimentalTreatments().isEmpty());
        assertTrue(evidence.preClinicalTreatments().isEmpty());
        assertTrue(evidence.knownResistantTreatments().isEmpty());
        assertTrue(evidence.suspectResistantTreatments().isEmpty());
    }

    @Test
    public void canFilterLowerLevelEvidence() {
        ActionableEvidence evidence = TestActionableEvidenceFactory.builder()
                .addApprovedTreatments("approved")
                .addOnLabelExperimentalTreatments("approved")
                .addOnLabelExperimentalTreatments("on-label experimental")
                .addOffLabelExperimentalTreatments("approved")
                .addOffLabelExperimentalTreatments("off-label experimental")
                .addPreClinicalTreatments("approved")
                .addPreClinicalTreatments("on-label experimental")
                .addPreClinicalTreatments("off-label experimental")
                .addPreClinicalTreatments("pre-clinical")
                .addKnownResistantTreatments("known resistant")
                .addSuspectResistantTreatments("known resistant")
                .addSuspectResistantTreatments("suspect resistant")
                .build();

        ActionableEvidence filtered = ActionableEvidenceFactory.filterRedundantLowerEvidence(evidence);
        assertEquals(1, filtered.approvedTreatments().size());
        assertTrue(filtered.approvedTreatments().contains("approved"));

        assertEquals(1, filtered.onLabelExperimentalTreatments().size());
        assertTrue(filtered.onLabelExperimentalTreatments().contains("on-label experimental"));

        assertEquals(1, filtered.offLabelExperimentalTreatments().size());
        assertTrue(filtered.offLabelExperimentalTreatments().contains("off-label experimental"));

        assertEquals(1, filtered.preClinicalTreatments().size());
        assertTrue(filtered.preClinicalTreatments().contains("pre-clinical"));

        assertEquals(1, filtered.knownResistantTreatments().size());
        assertTrue(filtered.knownResistantTreatments().contains("known resistant"));

        assertEquals(1, filtered.suspectResistantTreatments().size());
        assertTrue(filtered.suspectResistantTreatments().contains("suspect resistant"));
    }

    @NotNull
    private static ActionableEvent evidence(@NotNull String treatment, @NotNull EvidenceLevel level, @NotNull EvidenceDirection direction) {
        return TestServeActionabilityFactory.actionableGeneBuilder()
                .treatment(TestServeActionabilityFactory.treatmentBuilder().name(treatment).build())
                .source(ActionabilityConstants.EVIDENCE_SOURCE)
                .level(level)
                .direction(direction)
                .build();
    }
    @NotNull
    private static ActionableEvent trial(@NotNull String treatment, @NotNull EvidenceDirection direction) {
        return TestServeActionabilityFactory.actionableGeneBuilder()
                .treatment(TestServeActionabilityFactory.treatmentBuilder().name(treatment).build())
                .source(ActionabilityConstants.EXTERNAL_TRIAL_SOURCE)
                .direction(direction)
                .build();
    }
}