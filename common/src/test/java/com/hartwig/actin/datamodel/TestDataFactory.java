package com.hartwig.actin.datamodel;

import com.hartwig.actin.datamodel.clinical.TestClinicalDataFactory;

import org.jetbrains.annotations.NotNull;

public final class TestDataFactory {

    public static final String TEST_SAMPLE = "ACTN01029999T";

    private TestDataFactory() {
    }

    @NotNull
    public static ActinAnalysis createTestActinAnalysis() {
        return ImmutableActinAnalysis.builder().clinical(TestClinicalDataFactory.createProperTestClinicalRecord()).build();
    }
}
