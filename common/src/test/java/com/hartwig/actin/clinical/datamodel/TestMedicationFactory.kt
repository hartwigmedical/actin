package com.hartwig.actin.clinical.datamodel;

import org.apache.logging.log4j.util.Strings;
import org.jetbrains.annotations.NotNull;

public final class TestMedicationFactory {

    private TestMedicationFactory() {
    }

    @NotNull
    public static ImmutableMedication.Builder builder() {
        return ImmutableMedication.builder()
                .name(Strings.EMPTY)
                .qtProlongatingRisk(QTProlongatingRisk.NONE)
                .dosage(ImmutableDosage.builder().build())
                .isSelfCare(false)
                .isTrialMedication(false);
    }
}
