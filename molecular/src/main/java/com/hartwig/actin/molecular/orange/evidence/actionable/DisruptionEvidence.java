package com.hartwig.actin.molecular.orange.evidence.actionable;

import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.actin.molecular.orange.datamodel.linx.LinxDisruption;
import com.hartwig.serve.datamodel.ActionableEvent;
import com.hartwig.serve.datamodel.ActionableEvents;

import org.jetbrains.annotations.NotNull;

public class DisruptionEvidence {

    @NotNull
    public static DisruptionEvidence create(@NotNull ActionableEvents actionableEvents) {
        return new DisruptionEvidence();
    }

    @NotNull
    public List<ActionableEvent> findMatches(@NotNull LinxDisruption disruption) {
        List<ActionableEvent> applicableEvents = Lists.newArrayList();

        return applicableEvents;
    }
}
