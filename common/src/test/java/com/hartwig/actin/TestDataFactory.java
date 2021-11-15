package com.hartwig.actin;

import com.hartwig.actin.clinical.datamodel.TestClinicalDataFactory;
import com.hartwig.actin.molecular.datamodel.TestMolecularDataFactory;

import org.jetbrains.annotations.NotNull;

public final class TestDataFactory {

    public static final String TEST_SAMPLE = "ACTN01029999T";

    private TestDataFactory() {
    }

    @NotNull
    public static PatientRecord createMinimalTestPatientRecord() {
        return ImmutablePatientRecord.builder()
                .sampleId(TEST_SAMPLE)
                .clinical(TestClinicalDataFactory.createMinimalTestClinicalRecord())
                .molecular(TestMolecularDataFactory.createMinimalTestMolecularRecord())
                .build();
    }

    @NotNull
    public static PatientRecord createProperTestPatientRecord() {
        return ImmutablePatientRecord.builder()
                .from(createMinimalTestPatientRecord())
                .clinical(TestClinicalDataFactory.createProperTestClinicalRecord())
                .molecular(TestMolecularDataFactory.createProperTestMolecularRecord())
                .build();
    }
}
