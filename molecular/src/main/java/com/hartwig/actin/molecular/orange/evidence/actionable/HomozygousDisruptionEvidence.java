package com.hartwig.actin.molecular.orange.evidence.actionable;

import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.actin.molecular.orange.datamodel.linx.LinxHomozygousDisruption;
import com.hartwig.serve.datamodel.ActionableEvent;
import com.hartwig.serve.datamodel.ActionableEvents;

import org.jetbrains.annotations.NotNull;

public class HomozygousDisruptionEvidence {

    @NotNull
    public static HomozygousDisruptionEvidence create(@NotNull ActionableEvents actionableEvents) {
        return new HomozygousDisruptionEvidence();
    }

    @NotNull
    public List<ActionableEvent> findMatches(@NotNull LinxHomozygousDisruption homozygousDisruption) {
        List<ActionableEvent> applicableEvents = Lists.newArrayList();

        return applicableEvents;
    }
}
