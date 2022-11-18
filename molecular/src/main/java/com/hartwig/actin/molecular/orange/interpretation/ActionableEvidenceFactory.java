package com.hartwig.actin.molecular.orange.interpretation;

import java.util.List;
import java.util.Set;

import com.google.common.collect.Sets;
import com.hartwig.actin.molecular.datamodel.evidence.ActionableEvidence;
import com.hartwig.actin.molecular.datamodel.evidence.ImmutableActionableEvidence;
import com.hartwig.actin.molecular.orange.evidence.actionable.ActionabilityMatch;
import com.hartwig.serve.datamodel.ActionableEvent;

import org.jetbrains.annotations.NotNull;

public final class ActionableEvidenceFactory {

    private ActionableEvidenceFactory() {
    }

    @NotNull
    public static ActionableEvidence create(@NotNull ActionabilityMatch actionabilityMatch) {
        ActionableEvidence onLabelEvidence = createOnLabelEvidence(actionabilityMatch.onLabelEvents());
        ActionableEvidence offLabelEvidence = createOffLabelEvidence(actionabilityMatch.offLabelEvents());

        ActionableEvidence merged = ImmutableActionableEvidence.builder().from(onLabelEvidence).from(offLabelEvidence).build();

        return cleanResistanceEvidence(merged);
    }

    @NotNull
    private static ActionableEvidence createOnLabelEvidence(@NotNull List<ActionableEvent> onLabelEvents) {
        ImmutableActionableEvidence.Builder builder = ImmutableActionableEvidence.builder();

        for (ActionableEvent onLabelEvent : onLabelEvents) {
            if (onLabelEvent.direction().isResponsive()) {
                populateResponsiveOnLabelEvidence(builder, onLabelEvent);
            } else {
                populateResistantEvidence(builder, onLabelEvent);
            }
        }

        return builder.build();
    }

    @NotNull
    private static ActionableEvidence createOffLabelEvidence(@NotNull List<ActionableEvent> offLabelEvents) {
        ImmutableActionableEvidence.Builder builder = ImmutableActionableEvidence.builder();

        for (ActionableEvent offLabelEvent : offLabelEvents) {
            if (offLabelEvent.direction().isResponsive()) {
                populateResponsiveOffLabelEvidence(builder, offLabelEvent);
            } else {
                populateResistantEvidence(builder, offLabelEvent);
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
            @NotNull ActionableEvent onLabelResistantEvent) {
        String treatment = onLabelResistantEvent.treatment().name();
        switch (onLabelResistantEvent.level()) {
            case A:
            case B: {
                if (onLabelResistantEvent.direction().isCertain()) {
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
    private static ActionableEvidence cleanResistanceEvidence(@NotNull ActionableEvidence evidence) {
        Set<String> treatmentsToInclude = Sets.newHashSet();
        treatmentsToInclude.addAll(evidence.approvedTreatments());
        treatmentsToInclude.addAll(evidence.onLabelExperimentalTreatments());
        treatmentsToInclude.addAll(evidence.offLabelExperimentalTreatments());

        Set<String> applicableKnownResistantTreatments = filterTreatments(evidence.knownResistantTreatments(), treatmentsToInclude);
        Set<String> applicableSuspectResistantTreatments = filterTreatments(evidence.suspectResistantTreatments(), treatmentsToInclude);

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
}
