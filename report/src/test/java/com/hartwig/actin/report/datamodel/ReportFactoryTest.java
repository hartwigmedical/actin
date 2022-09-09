package com.hartwig.actin.report.datamodel;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import com.hartwig.actin.algo.datamodel.ImmutableTreatmentMatch;
import com.hartwig.actin.algo.datamodel.TestTreatmentMatchFactory;
import com.hartwig.actin.algo.datamodel.TreatmentMatch;
import com.hartwig.actin.clinical.datamodel.ClinicalRecord;
import com.hartwig.actin.clinical.datamodel.ImmutableClinicalRecord;
import com.hartwig.actin.clinical.datamodel.TestClinicalFactory;
import com.hartwig.actin.molecular.datamodel.MolecularRecord;
import com.hartwig.actin.molecular.datamodel.TestMolecularFactory;

import org.junit.Test;

public class ReportFactoryTest {

    @Test
    public void canCreateReportFromTestData() {
        assertNotNull(ReportFactory.fromInputs(TestClinicalFactory.createMinimalTestClinicalRecord(),
                TestMolecularFactory.createMinimalTestMolecularRecord(),
                TestTreatmentMatchFactory.createMinimalTreatmentMatch()));

        assertNotNull(ReportFactory.fromInputs(TestClinicalFactory.createProperTestClinicalRecord(),
                TestMolecularFactory.createProperTestMolecularRecord(),
                TestTreatmentMatchFactory.createProperTreatmentMatch()));
    }

    @Test
    public void usePatientSampleIdOnMismatch() {
        ClinicalRecord clinical = ImmutableClinicalRecord.builder()
                .from(TestClinicalFactory.createMinimalTestClinicalRecord())
                .patientId("clinical")
                .build();

        MolecularRecord molecular = TestMolecularFactory.createMinimalTestMolecularRecord();

        TreatmentMatch treatmentMatch = ImmutableTreatmentMatch.builder()
                .from(TestTreatmentMatchFactory.createMinimalTreatmentMatch())
                .sampleId("treatment-match")
                .build();

        assertEquals("clinical", ReportFactory.fromInputs(clinical, molecular, treatmentMatch).sampleId());
    }
}