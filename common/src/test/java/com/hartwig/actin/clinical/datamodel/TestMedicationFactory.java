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
                .codeATC(Strings.EMPTY)
                .chemicalSubgroupAtc(Strings.EMPTY)
                .pharmacologicalSubgroupAtc(Strings.EMPTY)
                .therapeuticSubgroupAtc(Strings.EMPTY)
                .anatomicalMainGroupAtc(Strings.EMPTY)
                .qtProlongatingRisk(QTProlongatingRisk.NONE)
                .dosage(ImmutableDosage.builder().build());
    }
}
