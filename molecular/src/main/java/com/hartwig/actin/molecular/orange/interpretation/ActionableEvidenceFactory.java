package com.hartwig.actin.molecular.orange.interpretation;

import java.util.List;

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
                populateResistantOnLabelEvidence(builder, onLabelEvent);
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
        }
    }

    private static void populateResistantOnLabelEvidence(@NotNull ImmutableActionableEvidence.Builder builder,
            @NotNull ActionableEvent onLabelResistantEvent) {
    }

    @NotNull
    private static ActionableEvidence createOffLabelEvidence(@NotNull List<ActionableEvent> offLabelEvents) {
        ImmutableActionableEvidence.Builder builder = ImmutableActionableEvidence.builder();

        return builder.build();
    }

    @NotNull
    private static ActionableEvidence cleanResistanceEvidence(@NotNull ActionableEvidence evidence) {
        // TODO
        return evidence;
    }
}
