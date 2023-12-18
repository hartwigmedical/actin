package com.hartwig.actin;

import com.hartwig.actin.clinical.datamodel.TestClinicalFactory;
import com.hartwig.actin.molecular.datamodel.TestMolecularFactory;

import org.jetbrains.annotations.NotNull;

public final class TestDataFactory {

    public static final String TEST_PATIENT = "ACTN01029999";
    public static final String TEST_SAMPLE = TEST_PATIENT + "T";

    private TestDataFactory() {
    }

    @NotNull
    public static PatientRecord createMinimalTestPatientRecord() {
        return ImmutablePatientRecord.builder()
                .patientId(TEST_PATIENT)
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
