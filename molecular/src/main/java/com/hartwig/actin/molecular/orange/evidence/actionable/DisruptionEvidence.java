package com.hartwig.actin.molecular.orange.evidence.actionable;

import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.actin.molecular.orange.datamodel.linx.LinxDisruption;
import com.hartwig.serve.datamodel.ActionableEvent;
import com.hartwig.serve.datamodel.ActionableEvents;

import org.jetbrains.annotations.NotNull;

public final class DisruptionEvidence {

    private DisruptionEvidence() {
    }

    @NotNull
    public static List<ActionableEvent> findMatches(@NotNull ActionableEvents actionableEvents, @NotNull LinxDisruption disruption) {
        List<ActionableEvent> applicableEvents = Lists.newArrayList();

        return applicableEvents;
    }
}
