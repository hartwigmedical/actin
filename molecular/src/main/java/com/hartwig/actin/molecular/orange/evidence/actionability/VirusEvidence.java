package com.hartwig.actin.molecular.orange.evidence.actionability;

import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.actin.molecular.orange.datamodel.virus.VirusInterpretation;
import com.hartwig.actin.molecular.orange.datamodel.virus.VirusInterpreterEntry;
import com.hartwig.serve.datamodel.ActionableEvent;
import com.hartwig.serve.datamodel.ActionableEvents;
import com.hartwig.serve.datamodel.characteristic.ActionableCharacteristic;
import com.hartwig.serve.datamodel.characteristic.TumorCharacteristicType;

import org.jetbrains.annotations.NotNull;

class VirusEvidence implements EvidenceMatcher<VirusInterpreterEntry> {

    @NotNull
    private final List<ActionableEvent> hpvCharacteristics;
    @NotNull
    private final List<ActionableEvent> ebvCharacteristics;

    @NotNull
    public static VirusEvidence create(@NotNull ActionableEvents actionableEvents) {
        List<ActionableEvent> hpvCharacteristics = Lists.newArrayList();
        List<ActionableEvent> ebvCharacteristics = Lists.newArrayList();
        for (ActionableCharacteristic actionableCharacteristic : actionableEvents.characteristics()) {
            if (actionableCharacteristic.type() == TumorCharacteristicType.HPV_POSITIVE) {
                hpvCharacteristics.add(actionableCharacteristic);
            } else if (actionableCharacteristic.type() == TumorCharacteristicType.EBV_POSITIVE) {
                ebvCharacteristics.add(actionableCharacteristic);
            }
        }

        return new VirusEvidence(hpvCharacteristics, ebvCharacteristics);
    }

    private VirusEvidence(@NotNull final List<ActionableEvent> hpvCharacteristics,
            @NotNull final List<ActionableEvent> ebvCharacteristics) {
        this.hpvCharacteristics = hpvCharacteristics;
        this.ebvCharacteristics = ebvCharacteristics;
    }

    @NotNull
    @Override
    public List<ActionableEvent> findMatches(@NotNull VirusInterpreterEntry virus) {
        VirusInterpretation interpretation = virus.interpretation();
        if (interpretation == null || !virus.reported()) {
            return Lists.newArrayList();
        }

        switch (interpretation) {
            case HPV: {
                return hpvCharacteristics;
            }
            case EBV: {
                return ebvCharacteristics;
            }
            default: {
                return Lists.newArrayList();
            }
        }
    }
}
