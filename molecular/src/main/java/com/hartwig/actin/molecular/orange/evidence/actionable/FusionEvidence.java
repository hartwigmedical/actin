package com.hartwig.actin.molecular.orange.evidence.actionable;

import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.actin.molecular.orange.datamodel.linx.LinxFusion;
import com.hartwig.serve.datamodel.ActionableEvent;
import com.hartwig.serve.datamodel.ActionableEvents;

import org.jetbrains.annotations.NotNull;

public class FusionEvidence {

    @NotNull
    public static FusionEvidence create(@NotNull ActionableEvents actionableEvents) {
        return new FusionEvidence();
    }

    @NotNull
    public  List<ActionableEvent> findMatches( @NotNull LinxFusion fusion) {
        List<ActionableEvent> applicableEvents = Lists.newArrayList();

        return applicableEvents;
    }
}
