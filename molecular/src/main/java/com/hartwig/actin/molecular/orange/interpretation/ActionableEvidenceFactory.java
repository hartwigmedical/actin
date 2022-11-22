package com.hartwig.actin.molecular.orange.interpretation;

import java.util.List;
import java.util.Set;

import com.google.common.collect.Sets;
import com.hartwig.actin.molecular.datamodel.evidence.ActionableEvidence;
import com.hartwig.actin.molecular.datamodel.evidence.ImmutableActionableEvidence;
import com.hartwig.actin.molecular.orange.evidence.actionability.ActionabilityConstants;
import com.hartwig.actin.molecular.orange.evidence.actionability.ActionabilityMatch;
import com.hartwig.serve.datamodel.ActionableEvent;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class ActionableEvidenceFactory {

    private ActionableEvidenceFactory() {
    }

    @Nullable
    public static ActionableEvidence create(@Nullable ActionabilityMatch actionabilityMatch) {
        if (actionabilityMatch == null) {
            return null;
        }

        ActionableEvidence onLabelEvidence = createOnLabelEvidence(actionabilityMatch.onLabelEvents());
        ActionableEvidence offLabelEvidence = createOffLabelEvidence(actionabilityMatch.offLabelEvents());
        ActionableEvidence externalTrialEvidence = createExternalTrialEvidence(actionabilityMatch.onLabelEvents());

        ActionableEvidence merged =
                ImmutableActionableEvidence.builder().from(onLabelEvidence).from(offLabelEvidence).from(externalTrialEvidence).build();

        ActionableEvidence simplified = filterRedundantLowerEvidence(merged);

        return filterResistanceEvidence(simplified);
    }

    @NotNull
    private static ActionableEvidence createOnLabelEvidence(@NotNull List<ActionableEvent> onLabelEvents) {
        ImmutableActionableEvidence.Builder builder = ImmutableActionableEvidence.builder();

        for (ActionableEvent onLabelEvent : onLabelEvents) {
            if (onLabelEvent.source() == ActionabilityConstants.EVIDENCE_SOURCE) {
                if (onLabelEvent.direction().isResponsive()) {
                    populateResponsiveOnLabelEvidence(builder, onLabelEvent);
                } else {
                    populateResistantEvidence(builder, onLabelEvent);
                }
            }
        }

        return builder.build();
    }

    @NotNull
    private static ActionableEvidence createOffLabelEvidence(@NotNull List<ActionableEvent> offLabelEvents) {
        ImmutableActionableEvidence.Builder builder = ImmutableActionableEvidence.builder();

        for (ActionableEvent offLabelEvent : offLabelEvents) {
            if (offLabelEvent.source() == ActionabilityConstants.EVIDENCE_SOURCE) {
                if (offLabelEvent.direction().isResponsive()) {
                    populateResponsiveOffLabelEvidence(builder, offLabelEvent);
                } else {
                    populateResistantEvidence(builder, offLabelEvent);
                }
            }
        }

        return builder.build();
    }

    @NotNull
    private static ActionableEvidence createExternalTrialEvidence(@NotNull List<ActionableEvent> onLabelEvents) {
        ImmutableActionableEvidence.Builder builder = ImmutableActionableEvidence.builder();

        for (ActionableEvent onLabelEvent : onLabelEvents) {
            if (onLabelEvent.source() == ActionabilityConstants.EXTERNAL_TRIAL_SOURCE && onLabelEvent.direction().isResponsive()) {
                builder.addExternalEligibleTrials(onLabelEvent.treatment().name());
            }
        }

        return builder.build();
    }

    private static void populateResponsiveOnLabelEvidence(@NotNull ImmutableActionableEvidence.Builder builder,
            @NotNull ActionableEvent onLabelResponsiveEvent) {
        String treatment = onLabelResponsiveEvent.treatment().name();
        switch (onLabelResponsiveEvent.level()) {
            case A: {
                if (onLabelResponsiveEvent.direction().isCertain()) {
                    builder.addApprovedTreatments(treatment);
                } else {
                    builder.addOnLabelExperimentalTreatments(treatment);
                }
                break;
            }
            case B: {
                if (onLabelResponsiveEvent.direction().isCertain()) {
                    builder.addOnLabelExperimentalTreatments(treatment);
                } else {
                    builder.addPreClinicalTreatments(treatment);
                }
                break;
            }
            default: {
                builder.addPreClinicalTreatments(treatment);
            }
        }
    }

    private static void populateResponsiveOffLabelEvidence(@NotNull ImmutableActionableEvidence.Builder builder,
            @NotNull ActionableEvent offLabelResponsiveEvent) {
        String treatment = offLabelResponsiveEvent.treatment().name();
        switch (offLabelResponsiveEvent.level()) {
            case A: {
                builder.addOnLabelExperimentalTreatments(treatment);
                break;
            }
            case B: {
                if (offLabelResponsiveEvent.direction().isCertain()) {
                    builder.addOffLabelExperimentalTreatments(treatment);
                } else {
                    builder.addPreClinicalTreatments(treatment);
                }
                break;
            }
            default: {
                builder.addPreClinicalTreatments(treatment);
            }
        }
    }

    private static void populateResistantEvidence(@NotNull ImmutableActionableEvidence.Builder builder,
            @NotNull ActionableEvent resistanceEvent) {
        String treatment = resistanceEvent.treatment().name();
        switch (resistanceEvent.level()) {
            case A:
            case B: {
                if (resistanceEvent.direction().isCertain()) {
                    builder.addKnownResistantTreatments(treatment);
                } else {
                    builder.addSuspectResistantTreatments(treatment);
                }
                break;
            }
            default: {
                builder.addSuspectResistantTreatments(treatment);
            }
        }
    }

    @NotNull
    private static ActionableEvidence filterRedundantLowerEvidence(@NotNull ActionableEvidence evidence) {
        Set<String> treatmentsToExcludeForOnLabel = evidence.approvedTreatments();
        Set<String> cleanedOnLabelTreatments = cleanTreatments(evidence.onLabelExperimentalTreatments(), treatmentsToExcludeForOnLabel);

        Set<String> treatmentsToExcludeForOffLabel = Sets.newHashSet();
        treatmentsToExcludeForOffLabel.addAll(evidence.approvedTreatments());
        treatmentsToExcludeForOffLabel.addAll(evidence.onLabelExperimentalTreatments());

        Set<String> cleanedOffLabelTreatments = cleanTreatments(evidence.offLabelExperimentalTreatments(), treatmentsToExcludeForOffLabel);

        Set<String> treatmentsToExcludeForPreClinical = Sets.newHashSet();
        treatmentsToExcludeForPreClinical.addAll(evidence.approvedTreatments());
        treatmentsToExcludeForPreClinical.addAll(evidence.onLabelExperimentalTreatments());
        treatmentsToExcludeForPreClinical.addAll(evidence.offLabelExperimentalTreatments());

        Set<String> cleanedPreClinicalTreatments = cleanTreatments(evidence.preClinicalTreatments(), treatmentsToExcludeForPreClinical);

        Set<String> treatmentsToExcludeForSuspectResistant = Sets.newHashSet();
        treatmentsToExcludeForSuspectResistant.addAll(evidence.knownResistantTreatments());

        Set<String> cleanedSuspectResistantTreatments =
                cleanTreatments(evidence.suspectResistantTreatments(), treatmentsToExcludeForSuspectResistant);

        return ImmutableActionableEvidence.builder()
                .from(evidence)
                .onLabelExperimentalTreatments(cleanedOnLabelTreatments)
                .offLabelExperimentalTreatments(cleanedOffLabelTreatments)
                .preClinicalTreatments(cleanedPreClinicalTreatments)
                .suspectResistantTreatments(cleanedSuspectResistantTreatments)
                .build();
    }

    @NotNull
    private static ActionableEvidence filterResistanceEvidence(@NotNull ActionableEvidence evidence) {
        Set<String> treatmentsToIncludeForResistance = Sets.newHashSet();
        treatmentsToIncludeForResistance.addAll(evidence.approvedTreatments());
        treatmentsToIncludeForResistance.addAll(evidence.onLabelExperimentalTreatments());
        treatmentsToIncludeForResistance.addAll(evidence.offLabelExperimentalTreatments());

        Set<String> applicableKnownResistantTreatments =
                filterTreatments(evidence.knownResistantTreatments(), treatmentsToIncludeForResistance);
        Set<String> applicableSuspectResistantTreatments =
                filterTreatments(evidence.suspectResistantTreatments(), treatmentsToIncludeForResistance);

        return ImmutableActionableEvidence.builder()
                .from(evidence)
                .knownResistantTreatments(applicableKnownResistantTreatments)
                .suspectResistantTreatments(applicableSuspectResistantTreatments)
                .build();
    }

    @NotNull
    private static Set<String> filterTreatments(@NotNull Set<String> treatments, @NotNull Set<String> treatmentsToInclude) {
        Set<String> filtered = Sets.newHashSet();
        for (String treatment : treatments) {
            if (treatmentsToInclude.contains(treatment)) {
                filtered.add(treatment);
            }
        }
        return filtered;
    }

    @NotNull
    private static Set<String> cleanTreatments(@NotNull Set<String> treatments, @NotNull Set<String> treatmentsToExclude) {
        Set<String> cleaned = Sets.newHashSet();
        for (String treatment : treatments) {
            if (!treatmentsToExclude.contains(treatment)) {
                cleaned.add(treatment);
            }
        }
        return cleaned;
    }
}
