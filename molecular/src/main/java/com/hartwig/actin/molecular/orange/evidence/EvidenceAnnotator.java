package com.hartwig.actin.molecular.orange.evidence;

import java.util.List;

import com.hartwig.actin.molecular.orange.curation.ExternalTrialMapping;
import com.hartwig.serve.datamodel.ActionableEvents;
import com.hartwig.serve.datamodel.KnownEvents;

import org.jetbrains.annotations.NotNull;

public class EvidenceAnnotator {

    @NotNull
    private final KnownEvents knownEvents;
    @NotNull
    private final ActionableEvents actionableEvents;
    @NotNull
    private final List<ExternalTrialMapping> externalTrialMappings;

    public EvidenceAnnotator(@NotNull final KnownEvents knownEvents, @NotNull final ActionableEvents actionableEvents,
            @NotNull final List<ExternalTrialMapping> externalTrialMappings) {
        this.knownEvents = knownEvents;
        this.actionableEvents = actionableEvents;
        this.externalTrialMappings = externalTrialMappings;
    }
}
