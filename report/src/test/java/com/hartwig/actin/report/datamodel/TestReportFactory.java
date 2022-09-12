package com.hartwig.actin.report.datamodel;

import com.hartwig.actin.TestDataFactory;
import com.hartwig.actin.algo.datamodel.TestTreatmentMatchFactory;
import com.hartwig.actin.clinical.datamodel.TestClinicalFactory;
import com.hartwig.actin.molecular.datamodel.TestMolecularFactory;

import org.jetbrains.annotations.NotNull;

public final class TestReportFactory {

    private TestReportFactory() {
    }

    @NotNull
    public static Report createMinimalTestReport() {
        return ImmutableReport.builder()
                .patientId(TestDataFactory.TEST_PATIENT)
                .clinical(TestClinicalFactory.createMinimalTestClinicalRecord())
                .molecular(TestMolecularFactory.createMinimalTestMolecularRecord())
                .treatmentMatch(TestTreatmentMatchFactory.createMinimalTreatmentMatch())
                .build();
    }

    @NotNull
    public static Report createProperTestReport() {
        return ImmutableReport.builder()
                .from(createMinimalTestReport())
                .clinical(TestClinicalFactory.createProperTestClinicalRecord())
                .molecular(TestMolecularFactory.createProperTestMolecularRecord())
                .treatmentMatch(TestTreatmentMatchFactory.createProperTreatmentMatch())
                .build();
    }

    @NotNull
    public static Report createExhaustiveTestReport() {
        return ImmutableReport.builder()
                .from(createMinimalTestReport())
                .clinical(TestClinicalFactory.createProperTestClinicalRecord())
                .molecular(TestMolecularFactory.createExhaustiveTestMolecularRecord())
                .treatmentMatch(TestTreatmentMatchFactory.createProperTreatmentMatch())
                .build();
    }
}
