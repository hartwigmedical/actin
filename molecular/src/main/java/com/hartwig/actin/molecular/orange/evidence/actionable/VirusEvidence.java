package com.hartwig.actin.molecular.orange.evidence.actionable;

import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.actin.molecular.orange.datamodel.virus.VirusInterpreterEntry;
import com.hartwig.serve.datamodel.ActionableEvent;
import com.hartwig.serve.datamodel.ActionableEvents;

import org.jetbrains.annotations.NotNull;

public class VirusEvidence {

    @NotNull
    public static VirusEvidence create(@NotNull ActionableEvents actionableEvents) {
        return new VirusEvidence();
    }

    @NotNull
    public List<ActionableEvent> findMatches(@NotNull VirusInterpreterEntry virus) {
        List<ActionableEvent> applicableEvents = Lists.newArrayList();

        return applicableEvents;
    }
}
