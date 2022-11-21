package com.hartwig.actin.molecular.orange.evidence.actionable;

import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.actin.molecular.orange.datamodel.virus.VirusInterpreterEntry;
import com.hartwig.serve.datamodel.ActionableEvent;
import com.hartwig.serve.datamodel.ActionableEvents;
import com.hartwig.serve.datamodel.characteristic.TumorCharacteristicType;

import org.jetbrains.annotations.NotNull;

public final class VirusEvidence {

    private static final TumorCharacteristicType HPV_POSITIVE = TumorCharacteristicType.HPV_POSITIVE;

    private VirusEvidence() {
    }

    @NotNull
    public static List<ActionableEvent> findMatches(@NotNull ActionableEvents actionableEvents, @NotNull VirusInterpreterEntry virus) {
        List<ActionableEvent> applicableEvents = Lists.newArrayList();

        return applicableEvents;
    }
}
