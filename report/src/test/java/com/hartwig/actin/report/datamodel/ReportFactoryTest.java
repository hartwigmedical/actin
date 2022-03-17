package com.hartwig.actin.report.datamodel;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import com.hartwig.actin.algo.datamodel.ImmutableTreatmentMatch;
import com.hartwig.actin.algo.datamodel.TestTreatmentMatchFactory;
import com.hartwig.actin.algo.datamodel.TreatmentMatch;
import com.hartwig.actin.clinical.datamodel.ClinicalRecord;
import com.hartwig.actin.clinical.datamodel.ImmutableClinicalRecord;
import com.hartwig.actin.clinical.datamodel.TestClinicalDataFactory;
import com.hartwig.actin.molecular.datamodel.MolecularRecord;
import com.hartwig.actin.molecular.datamodel.TestMolecularDataFactory;

import org.junit.Test;

public class ReportFactoryTest {

    @Test
    public void canCreateReportFromTestData() {
        assertNotNull(ReportFactory.fromInputs(TestClinicalDataFactory.createMinimalTestClinicalRecord(),
                TestMolecularDataFactory.createMinimalTestMolecularRecord(),
                TestTreatmentMatchFactory.createMinimalTreatmentMatch()));

        assertNotNull(ReportFactory.fromInputs(TestClinicalDataFactory.createProperTestClinicalRecord(),
                TestMolecularDataFactory.createProperTestMolecularRecord(),
                TestTreatmentMatchFactory.createProperTreatmentMatch()));
    }

    @Test
    public void usePatientSampleIdOnMismatch() {
        ClinicalRecord clinical = ImmutableClinicalRecord.builder()
                .from(TestClinicalDataFactory.createMinimalTestClinicalRecord())
                .sampleId("clinical")
                .build();

        MolecularRecord molecular = TestMolecularDataFactory.createMinimalTestMolecularRecord();

        TreatmentMatch treatmentMatch = ImmutableTreatmentMatch.builder()
                .from(TestTreatmentMatchFactory.createMinimalTreatmentMatch())
                .sampleId("treatment-match")
                .build();

        assertEquals("clinical", ReportFactory.fromInputs(clinical, molecular, treatmentMatch).sampleId());
    }
}