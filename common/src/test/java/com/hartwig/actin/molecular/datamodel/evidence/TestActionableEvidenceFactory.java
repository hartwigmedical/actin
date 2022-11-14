package com.hartwig.actin.molecular.datamodel.evidence;

import org.jetbrains.annotations.NotNull;

public final class TestActionableEvidenceFactory {

    private TestActionableEvidenceFactory() {
    }

    @NotNull
    public static ActionableEvidence createExhaustive() {
        return ImmutableActionableEvidence.builder()
                .addApprovedTreatments("approved")
                .addExternalEligibleTrials("external trial")
                .addOnLabelExperimentalTreatments("on-label experimental")
                .addOffLabelExperimentalTreatments("off-label experimental")
                .addPreClinicalTreatments("pre-clinical")
                .addKnownResistantTreatments("known resistant")
                .addSuspectResistantTreatments("suspect resistant")
                .build();
    }

    @NotNull
    public static ActionableEvidence withApprovedTreatment(@NotNull String treatment) {
        return ImmutableActionableEvidence.builder().addApprovedTreatments(treatment).build();
    }

    @NotNull
    public static ActionableEvidence withPreClinicalTreatment(@NotNull String treatment) {
        return ImmutableActionableEvidence.builder().addPreClinicalTreatments(treatment).build();
    }
}
