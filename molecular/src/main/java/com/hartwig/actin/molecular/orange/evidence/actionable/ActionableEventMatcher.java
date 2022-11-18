package com.hartwig.actin.molecular.orange.evidence.actionable;

import java.util.List;

import com.hartwig.actin.molecular.orange.curation.ExternalTrialMapping;
import com.hartwig.actin.molecular.orange.datamodel.linx.LinxDisruption;
import com.hartwig.actin.molecular.orange.datamodel.linx.LinxFusion;
import com.hartwig.actin.molecular.orange.datamodel.linx.LinxHomozygousDisruption;
import com.hartwig.actin.molecular.orange.datamodel.purple.PurpleCopyNumber;
import com.hartwig.actin.molecular.orange.datamodel.purple.PurpleVariant;
import com.hartwig.actin.molecular.orange.datamodel.virus.VirusInterpreterEntry;
import com.hartwig.serve.datamodel.ActionableEvent;
import com.hartwig.serve.datamodel.ActionableEvents;

import org.jetbrains.annotations.NotNull;

public class ActionableEventMatcher {

    @NotNull
    private final ActionableEvents actionableEvents;
    @NotNull
    private final List<ExternalTrialMapping> externalTrialMappings;

    public ActionableEventMatcher(@NotNull final ActionableEvents actionableEvents,
            @NotNull final List<ExternalTrialMapping> externalTrialMappings) {
        this.actionableEvents = actionableEvents;
        this.externalTrialMappings = externalTrialMappings;
    }

    @NotNull
    public ActionabilityMatch matchForVariant(@NotNull PurpleVariant variant) {
        return createMatchResult(VariantEvidence.findMatches(actionableEvents, variant));
    }

    @NotNull
    public ActionabilityMatch matchForCopyNumber(@NotNull PurpleCopyNumber copyNumber) {
        return ImmutableActionabilityMatch.builder().build();
    }

    @NotNull
    public ActionabilityMatch matchForHomozygousDisruption(@NotNull LinxHomozygousDisruption homozygousDisruption) {
        return ImmutableActionabilityMatch.builder().build();
    }

    @NotNull
    public ActionabilityMatch matchForDisruption(@NotNull LinxDisruption disruption) {
        return ImmutableActionabilityMatch.builder().build();
    }

    @NotNull
    public ActionabilityMatch matchForFusion(@NotNull LinxFusion fusion) {
        return ImmutableActionabilityMatch.builder().build();
    }

    @NotNull
    public ActionabilityMatch matchForVirus(@NotNull VirusInterpreterEntry virus) {
        return ImmutableActionabilityMatch.builder().build();
    }

    @NotNull
    private ActionabilityMatch createMatchResult(@NotNull List<ActionableEvent> matches) {
        // TODO Split on-label and off-label
        // TODO Rename external trials based on mapping.

        List<ActionableEvent> curated = curateExternalTrials(matches);

        return ImmutableActionabilityMatch.builder().onLabelEvents(matches).build();
    }

    @NotNull
    private List<ActionableEvent> curateExternalTrials(@NotNull List<ActionableEvent> matches) {
        return matches;
    }
}
