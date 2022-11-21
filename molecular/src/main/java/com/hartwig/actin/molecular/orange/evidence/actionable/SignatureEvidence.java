package com.hartwig.actin.molecular.orange.evidence.actionable;

import java.util.List;

import com.google.common.collect.Lists;
import com.hartwig.serve.datamodel.ActionableEvent;
import com.hartwig.serve.datamodel.ActionableEvents;
import com.hartwig.serve.datamodel.characteristic.ActionableCharacteristic;
import com.hartwig.serve.datamodel.characteristic.TumorCharacteristicType;

import org.jetbrains.annotations.NotNull;

class SignatureEvidence {

    private static final TumorCharacteristicType MICROSATELLITE_UNSTABLE_TYPE = TumorCharacteristicType.MICROSATELLITE_UNSTABLE;
    private static final TumorCharacteristicType HOMOLOGOUS_REPAIR_DEFICIENT_TYPE =
            TumorCharacteristicType.HOMOLOGOUS_RECOMBINATION_DEFICIENT;
    private static final TumorCharacteristicType HIGH_TUMOR_MUTATIONAL_BURDEN_TYPE = TumorCharacteristicType.HIGH_TUMOR_MUTATIONAL_BURDEN;
    private static final TumorCharacteristicType HIGH_TUMOR_MUTATIONAL_LOAD_TYPE = TumorCharacteristicType.HIGH_TUMOR_MUTATIONAL_LOAD;

    @NotNull
    private final List<ActionableCharacteristic> signatureCharacteristics;

    @NotNull
    public static SignatureEvidence create(@NotNull ActionableEvents actionableEvents) {
        List<ActionableCharacteristic> signatureCharacteristics = Lists.newArrayList();
        for (ActionableCharacteristic actionableCharacteristic : actionableEvents.characteristics()) {
            if (actionableCharacteristic.type() == MICROSATELLITE_UNSTABLE_TYPE
                    || actionableCharacteristic.type() == HOMOLOGOUS_REPAIR_DEFICIENT_TYPE
                    || actionableCharacteristic.type() == HIGH_TUMOR_MUTATIONAL_BURDEN_TYPE
                    || actionableCharacteristic.type() == HIGH_TUMOR_MUTATIONAL_LOAD_TYPE) {
                signatureCharacteristics.add(actionableCharacteristic);
            }
        }

        return new SignatureEvidence(signatureCharacteristics);
    }

    private SignatureEvidence(@NotNull final List<ActionableCharacteristic> signatureCharacteristics) {
        this.signatureCharacteristics = signatureCharacteristics;
    }

    @NotNull
    public List<ActionableEvent> findMicrosatelliteMatches(boolean isMicrosatelliteUnstable) {
        return findMatches(isMicrosatelliteUnstable, MICROSATELLITE_UNSTABLE_TYPE);
    }

    @NotNull
    public List<ActionableEvent> findHomologousRepairMatches(boolean isHomologousRepairDeficient) {
        return findMatches(isHomologousRepairDeficient, HOMOLOGOUS_REPAIR_DEFICIENT_TYPE);
    }

    @NotNull
    public List<ActionableEvent> findTumorBurdenMatches(boolean hasHighTumorMutationalBurden) {
        return findMatches(hasHighTumorMutationalBurden, HIGH_TUMOR_MUTATIONAL_BURDEN_TYPE);
    }

    @NotNull
    public List<ActionableEvent> findTumorLoadMatches(boolean hasHighTumorMutationalLoad) {
        return findMatches(hasHighTumorMutationalLoad, HIGH_TUMOR_MUTATIONAL_LOAD_TYPE);
    }

    @NotNull
    private List<ActionableEvent> findMatches(boolean hasCharacteristic, @NotNull TumorCharacteristicType typeToFind) {
        if (!hasCharacteristic) {
            return Lists.newArrayList();
        }

        List<ActionableEvent> applicableEvents = Lists.newArrayList();

        for (ActionableCharacteristic actionableCharacteristic : signatureCharacteristics) {
            if (actionableCharacteristic.type() == typeToFind) {
                applicableEvents.add(actionableCharacteristic);
            }
        }
        return applicableEvents;
    }
}
