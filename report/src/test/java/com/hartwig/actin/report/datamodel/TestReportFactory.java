package com.hartwig.actin.report.datamodel;

import com.hartwig.actin.TestDataFactory;
import com.hartwig.actin.algo.datamodel.TestTreatmentMatchFactory;
import com.hartwig.actin.clinical.datamodel.TestClinicalDataFactory;
import com.hartwig.actin.molecular.datamodel.TestMolecularDataFactory;

import org.jetbrains.annotations.NotNull;

public final class TestReportFactory {

    private TestReportFactory() {
    }

    @NotNull
    public static Report createMinimalTestReport() {
        return ImmutableReport.builder()
                .sampleId(TestDataFactory.TEST_SAMPLE)
                .clinical(TestClinicalDataFactory.createMinimalTestClinicalRecord())
                .molecular(TestMolecularDataFactory.createMinimalTestMolecularRecord())
                .treatmentMatch(TestTreatmentMatchFactory.createMinimalTreatmentMatch())
                .build();
    }

    @NotNull
    public static Report createProperTestReport() {
        return ImmutableReport.builder()
                .from(createMinimalTestReport())
                .clinical(TestClinicalDataFactory.createProperTestClinicalRecord())
                .molecular(TestMolecularDataFactory.createProperTestMolecularRecord())
                .treatmentMatch(TestTreatmentMatchFactory.createProperTreatmentMatch())
                .build();
    }

    @NotNull
    public static Report createExhaustiveTestReport() {
        return ImmutableReport.builder()
                .from(createMinimalTestReport())
                .clinical(TestClinicalDataFactory.createProperTestClinicalRecord())
                .molecular(TestMolecularDataFactory.createExhaustiveTestMolecularRecord())
                .treatmentMatch(TestTreatmentMatchFactory.createProperTreatmentMatch())
                .build();
    }
}
