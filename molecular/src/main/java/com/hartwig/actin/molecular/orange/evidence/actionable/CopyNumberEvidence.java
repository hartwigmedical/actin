package com.hartwig.actin.molecular.orange.evidence.actionable;

import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.actin.molecular.orange.datamodel.purple.PurpleCopyNumber;
import com.hartwig.serve.datamodel.ActionableEvent;
import com.hartwig.serve.datamodel.ActionableEvents;

import org.jetbrains.annotations.NotNull;

public class CopyNumberEvidence {

    @NotNull
    public static CopyNumberEvidence create(@NotNull ActionableEvents actionableEvents) {
        return new CopyNumberEvidence();
    }

    @NotNull
    public List<ActionableEvent> findMatches( @NotNull PurpleCopyNumber copyNumber) {
        List<ActionableEvent> applicableEvents = Lists.newArrayList();

        return applicableEvents;
    }
}
