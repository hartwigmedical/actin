package com.hartwig.actin;

import com.hartwig.actin.clinical.datamodel.TestClinicalFactory;
import com.hartwig.actin.molecular.datamodel.TestMolecularFactory;

import org.jetbrains.annotations.NotNull;

public final class TestDataFactory {

    public static final String TEST_SAMPLE = "ACTN01029999T";

    private TestDataFactory() {
    }

    @NotNull
    public static PatientRecord createMinimalTestPatientRecord() {
        return ImmutablePatientRecord.builder()
                .sampleId(TEST_SAMPLE)
                .clinical(TestClinicalFactory.createMinimalTestClinicalRecord())
                .molecular(TestMolecularFactory.createMinimalTestMolecularRecord())
                .build();
    }

    @NotNull
    public static PatientRecord createProperTestPatientRecord() {
        return ImmutablePatientRecord.builder()
                .from(createMinimalTestPatientRecord())
                .clinical(TestClinicalFactory.createProperTestClinicalRecord())
                .molecular(TestMolecularFactory.createProperTestMolecularRecord())
                .build();
    }
}
