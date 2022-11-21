package com.hartwig.actin.molecular.orange.evidence.actionable;

import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.actin.molecular.orange.datamodel.purple.PurpleCopyNumber;
import com.hartwig.serve.datamodel.ActionableEvent;
import com.hartwig.serve.datamodel.ActionableEvents;

import org.jetbrains.annotations.NotNull;

public final class CopyNumberEvidence {

    private CopyNumberEvidence() {
    }

    @NotNull
    public static List<ActionableEvent> findMatches(@NotNull ActionableEvents actionableEvents, @NotNull PurpleCopyNumber copyNumber) {
        List<ActionableEvent> applicableEvents = Lists.newArrayList();

        return applicableEvents;
    }
}
