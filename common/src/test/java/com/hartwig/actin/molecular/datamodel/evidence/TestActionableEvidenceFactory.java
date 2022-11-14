package com.hartwig.actin.molecular.datamodel.evidence;

import org.jetbrains.annotations.NotNull;

public final class TestActionableEvidenceFactory {

    private TestActionableEvidenceFactory() {
    }

    @NotNull
    public static ActionableEvidence createEmpty() {
        return ImmutableActionableEvidence.builder().build();
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
    public static ActionableEvidence withExternalEligibleTrial(@NotNull String treatment) {
        return ImmutableActionableEvidence.builder().addExternalEligibleTrials(treatment).build();
    }

    @NotNull
    public static ActionableEvidence withOnLabelExperimentalTreatment(@NotNull String treatment) {
        return ImmutableActionableEvidence.builder().addOnLabelExperimentalTreatments(treatment).build();
    }

    @NotNull
    public static ActionableEvidence withOffLabelExperimentalTreatment(@NotNull String treatment) {
        return ImmutableActionableEvidence.builder().addOffLabelExperimentalTreatments(treatment).build();
    }

    @NotNull
    public static ActionableEvidence withPreClinicalTreatment(@NotNull String treatment) {
        return ImmutableActionableEvidence.builder().addPreClinicalTreatments(treatment).build();
    }

    @NotNull
    public static ActionableEvidence withKnownResistantTreatment(@NotNull String treatment) {
        return ImmutableActionableEvidence.builder().addKnownResistantTreatments(treatment).build();
    }

    @NotNull
    public static ActionableEvidence withSuspectResistantTreatment(@NotNull String treatment) {
        return ImmutableActionableEvidence.builder().addSuspectResistantTreatments(treatment).build();
    }
}
