package com.hartwig.actin.molecular.datamodel.evidence;

import java.util.Set;

import com.google.common.collect.Sets;

import org.jetbrains.annotations.NotNull;

public final class TestActionableEvidenceFactory {

    private TestActionableEvidenceFactory() {
    }

    @NotNull
    public static ImmutableActionableEvidence.Builder builder() {
        return ImmutableActionableEvidence.builder();
    }

    @NotNull
    public static ActionableEvidence createEmpty() {
        return builder().build();
    }

    @NotNull
    public static ActionableEvidence createExhaustive() {
        return builder().addApprovedTreatments("approved")
                .addExternalEligibleTrials(ExternalTrialFactory.create("external trial",
                        Sets.newHashSet("Netherlands"),
                        "https://clinicaltrials.gov/study/NCT00000001"))
                .addOnLabelExperimentalTreatments("on-label experimental")
                .addOffLabelExperimentalTreatments("off-label experimental")
                .addPreClinicalTreatments("pre-clinical")
                .addKnownResistantTreatments("known resistant")
                .addSuspectResistantTreatments("suspect resistant")
                .build();
    }

    @NotNull
    public static ActionableEvidence withApprovedTreatment(@NotNull String treatment) {
        return builder().addApprovedTreatments(treatment).build();
    }

    @NotNull
    public static ActionableEvidence withExternalEligibleTrial(@NotNull String treatment, @NotNull Set<String> countries,
            @NotNull String website) {
        return builder().addExternalEligibleTrials(ExternalTrialFactory.create(treatment, countries, website)).build();
    }

    @NotNull
    public static ActionableEvidence withOnLabelExperimentalTreatment(@NotNull String treatment) {
        return builder().addOnLabelExperimentalTreatments(treatment).build();
    }

    @NotNull
    public static ActionableEvidence withOffLabelExperimentalTreatment(@NotNull String treatment) {
        return builder().addOffLabelExperimentalTreatments(treatment).build();
    }

    @NotNull
    public static ActionableEvidence withPreClinicalTreatment(@NotNull String treatment) {
        return builder().addPreClinicalTreatments(treatment).build();
    }

    @NotNull
    public static ActionableEvidence withKnownResistantTreatment(@NotNull String treatment) {
        return builder().addKnownResistantTreatments(treatment).build();
    }

    @NotNull
    public static ActionableEvidence withSuspectResistantTreatment(@NotNull String treatment) {
        return builder().addSuspectResistantTreatments(treatment).build();
    }
}
