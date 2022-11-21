package com.hartwig.actin.molecular.orange.evidence.actionable;

import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.serve.datamodel.ActionableEvent;
import com.hartwig.serve.datamodel.ActionableEvents;
import com.hartwig.serve.datamodel.characteristic.ActionableCharacteristic;
import com.hartwig.serve.datamodel.characteristic.TumorCharacteristicType;

import org.jetbrains.annotations.NotNull;

public final class CharacteristicsEvidence {

    private static final TumorCharacteristicType MICROSATELLITE_UNSTABLE_TYPE = TumorCharacteristicType.MICROSATELLITE_UNSTABLE;
    private static final TumorCharacteristicType HOMOLOGOUS_REPAIR_DEFICIENT_TYPE =
            TumorCharacteristicType.HOMOLOGOUS_RECOMBINATION_DEFICIENT;
    private static final TumorCharacteristicType HIGH_TUMOR_MUTATIONAL_BURDEN_TYPE = TumorCharacteristicType.HIGH_TUMOR_MUTATIONAL_BURDEN;
    private static final TumorCharacteristicType HIGH_TUMOR_MUTATIONAL_LOAD_TYPE = TumorCharacteristicType.HIGH_TUMOR_MUTATIONAL_LOAD;

    private CharacteristicsEvidence() {
    }

    @NotNull
    public static List<ActionableEvent> findMicrosatelliteMatches(@NotNull ActionableEvents actionableEvents,
            boolean isMicrosatelliteUnstable) {
        return findMatches(actionableEvents, isMicrosatelliteUnstable, MICROSATELLITE_UNSTABLE_TYPE);
    }

    @NotNull
    public static List<ActionableEvent> findHomologousRepairMatches(@NotNull ActionableEvents actionableEvents,
            boolean isHomologousRepairDeficient) {
        return findMatches(actionableEvents, isHomologousRepairDeficient, HOMOLOGOUS_REPAIR_DEFICIENT_TYPE);
    }

    @NotNull
    public static List<ActionableEvent> findTumorBurdenMatches(@NotNull ActionableEvents actionableEvents,
            boolean hasHighTumorMutationalBurden) {
        return findMatches(actionableEvents, hasHighTumorMutationalBurden, HIGH_TUMOR_MUTATIONAL_BURDEN_TYPE);
    }

    @NotNull
    public static List<ActionableEvent> findTumorLoadMatches(@NotNull ActionableEvents actionableEvents,
            boolean hasHighTumorMutationalLoad) {
        return findMatches(actionableEvents, hasHighTumorMutationalLoad, HIGH_TUMOR_MUTATIONAL_LOAD_TYPE);
    }

    @NotNull
    private static List<ActionableEvent> findMatches(@NotNull ActionableEvents actionableEvents, boolean hasCharacteristic,
            @NotNull TumorCharacteristicType typeToFind) {
        if (!hasCharacteristic) {
            return Lists.newArrayList();
        }

        List<ActionableEvent> applicableEvents = Lists.newArrayList();

        for (ActionableCharacteristic characteristic : actionableEvents.characteristics()) {
            if (characteristic.type() == typeToFind) {
                applicableEvents.add(characteristic);
            }
        }
        return applicableEvents;
    }
}
