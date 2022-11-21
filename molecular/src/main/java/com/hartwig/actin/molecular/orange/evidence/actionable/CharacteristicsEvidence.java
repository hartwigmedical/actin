package com.hartwig.actin.molecular.orange.evidence.actionable;

import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.serve.datamodel.ActionableEvent;
import com.hartwig.serve.datamodel.ActionableEvents;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class CharacteristicsEvidence {

    private CharacteristicsEvidence() {
    }

    @NotNull
    public static List<ActionableEvent> findMicrosatelliteMatches(@NotNull ActionableEvents actionableEvents,
            @Nullable Boolean isMicrosatelliteUnstable) {
        List<ActionableEvent> applicableEvents = Lists.newArrayList();

        return applicableEvents;
    }

    @NotNull
    public static List<ActionableEvent> findHomologousRepairMatches(@NotNull ActionableEvents actionableEvents,
            @Nullable Boolean isHomologousRepairDeficient) {
        List<ActionableEvent> applicableEvents = Lists.newArrayList();

        return applicableEvents;
    }

    @NotNull
    public static List<ActionableEvent> findHighTumorBurdenEvidence(@NotNull ActionableEvents actionableEvents,
            @Nullable Boolean hasHighTumorMutationalBurden) {
        List<ActionableEvent> applicableEvents = Lists.newArrayList();

        return applicableEvents;
    }

    @NotNull
    public static List<ActionableEvent> findHighTumorLoadEvidence(@NotNull ActionableEvents actionableEvents,
            @Nullable Boolean hasHighTumorMutationalLoad) {
        List<ActionableEvent> applicableEvents = Lists.newArrayList();

        return applicableEvents;
    }
}
