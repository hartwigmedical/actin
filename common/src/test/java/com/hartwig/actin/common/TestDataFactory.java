package com.hartwig.actin.common;

import com.hartwig.actin.clinical.datamodel.TestClinicalDataFactory;
import com.hartwig.actin.molecular.datamodel.TestMolecularDataFactory;

import org.jetbrains.annotations.NotNull;

public final class TestDataFactory {

    public static final String TEST_SAMPLE = "ACTN01029999T";

    private TestDataFactory() {
    }

    @NotNull
    public static ActinRecord createTestActinRecord() {
        return ImmutableActinRecord.builder()
                .clinical(TestClinicalDataFactory.createProperTestClinicalRecord())
                .molecular(TestMolecularDataFactory.createProperTestMolecularRecord())
                .build();
    }
}
