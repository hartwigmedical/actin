package com.hartwig.actin.molecular.orange.evidence.actionable;

import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.actin.molecular.orange.datamodel.virus.VirusInterpretation;
import com.hartwig.actin.molecular.orange.datamodel.virus.VirusInterpreterEntry;
import com.hartwig.serve.datamodel.ActionableEvent;
import com.hartwig.serve.datamodel.ActionableEvents;
import com.hartwig.serve.datamodel.characteristic.ActionableCharacteristic;
import com.hartwig.serve.datamodel.characteristic.TumorCharacteristicType;

import org.jetbrains.annotations.NotNull;

class VirusEvidence {

    @NotNull
    private final List<ActionableCharacteristic> virusCharacteristics;

    @NotNull
    public static VirusEvidence create(@NotNull ActionableEvents actionableEvents) {
        List<ActionableCharacteristic> virusCharacteristics = Lists.newArrayList();
        for (ActionableCharacteristic actionableCharacteristic : actionableEvents.characteristics()) {
            if (actionableCharacteristic.type() == TumorCharacteristicType.HPV_POSITIVE
                    || actionableCharacteristic.type() == TumorCharacteristicType.EBV_POSITIVE) {
                virusCharacteristics.add(actionableCharacteristic);
            }
        }

        return new VirusEvidence(virusCharacteristics);
    }

    private VirusEvidence(@NotNull final List<ActionableCharacteristic> virusCharacteristics) {
        this.virusCharacteristics = virusCharacteristics;
    }

    @NotNull
    public List<ActionableEvent> findMatches(@NotNull VirusInterpreterEntry virus) {
        VirusInterpretation interpretation = virus.interpretation();
        if (interpretation == null) {
            return Lists.newArrayList();
        }

        switch (interpretation) {
            case HPV: {
                return hpvCharacteristics(virusCharacteristics);
            }
            case EBV: {
                return ebvCharacteristics(virusCharacteristics);
            }
            default: {
                return Lists.newArrayList();
            }
        }
    }

    @NotNull
    private static List<ActionableEvent> hpvCharacteristics(@NotNull List<ActionableCharacteristic> virusCharacteristics) {
        return filter(virusCharacteristics, TumorCharacteristicType.HPV_POSITIVE);
    }

    @NotNull
    private static List<ActionableEvent> ebvCharacteristics(@NotNull List<ActionableCharacteristic> virusCharacteristics) {
        return filter(virusCharacteristics, TumorCharacteristicType.EBV_POSITIVE);
    }

    @NotNull
    private static List<ActionableEvent> filter(@NotNull List<ActionableCharacteristic> actionableCharacteristics,
            @NotNull TumorCharacteristicType typeToFind) {
        List<ActionableEvent> filtered = Lists.newArrayList();
        for (ActionableCharacteristic actionableCharacteristic : actionableCharacteristics) {
            if (actionableCharacteristic.type() == typeToFind) {
                filtered.add(actionableCharacteristic);
            }
        }
        return filtered;
    }
}
