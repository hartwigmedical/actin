package com.hartwig.actin.molecular.orange.evidence.actionable;

import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.actin.molecular.orange.datamodel.linx.LinxFusion;
import com.hartwig.serve.datamodel.ActionableEvent;
import com.hartwig.serve.datamodel.ActionableEvents;

import org.jetbrains.annotations.NotNull;

public final class FusionEvidence {

    private FusionEvidence() {
    }

    @NotNull
    public static List<ActionableEvent> findMatches(@NotNull ActionableEvents actionableEvents, @NotNull LinxFusion fusion) {
        List<ActionableEvent> applicableEvents = Lists.newArrayList();

        return applicableEvents;
    }
}
