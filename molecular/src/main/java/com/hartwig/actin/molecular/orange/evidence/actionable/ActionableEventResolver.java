package com.hartwig.actin.molecular.orange.evidence.actionable;

import java.util.List;

import com.hartwig.actin.molecular.orange.curation.ExternalTrialMapping;
import com.hartwig.actin.molecular.orange.datamodel.linx.LinxDisruption;
import com.hartwig.actin.molecular.orange.datamodel.linx.LinxFusion;
import com.hartwig.actin.molecular.orange.datamodel.linx.LinxHomozygousDisruption;
import com.hartwig.actin.molecular.orange.datamodel.purple.PurpleCopyNumber;
import com.hartwig.actin.molecular.orange.datamodel.purple.PurpleVariant;
import com.hartwig.actin.molecular.orange.datamodel.virus.VirusInterpreterEntry;
import com.hartwig.serve.datamodel.ActionableEvents;

import org.jetbrains.annotations.NotNull;

public class ActionableEventResolver {

    @NotNull
    private final ActionableEvents actionableEvents;
    @NotNull
    private final List<ExternalTrialMapping> externalTrialMappings;

    public ActionableEventResolver(@NotNull final ActionableEvents actionableEvents,
            @NotNull final List<ExternalTrialMapping> externalTrialMappings) {
        this.actionableEvents = actionableEvents;
        this.externalTrialMappings = externalTrialMappings;
    }

    @NotNull
    public ActionabilityMatch resolveForVariant(@NotNull PurpleVariant variant) {
        return ImmutableActionabilityMatch.builder().build();
    }

    @NotNull
    public ActionabilityMatch resolveForCopyNumber(@NotNull PurpleCopyNumber copyNumber) {
        return ImmutableActionabilityMatch.builder().build();
    }

    @NotNull
    public ActionabilityMatch resolveForHomozygousDisruption(@NotNull LinxHomozygousDisruption homozygousDisruption) {
        return ImmutableActionabilityMatch.builder().build();
    }

    @NotNull
    public ActionabilityMatch resolveForDisruption(@NotNull LinxDisruption disruption) {
        return ImmutableActionabilityMatch.builder().build();
    }

    @NotNull
    public ActionabilityMatch resolveForFusion(@NotNull LinxFusion fusion) {
        return ImmutableActionabilityMatch.builder().build();
    }

    @NotNull
    public ActionabilityMatch resolveForVirus(@NotNull VirusInterpreterEntry virus) {
        return ImmutableActionabilityMatch.builder().build();
    }
}
