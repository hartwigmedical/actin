package com.hartwig.actin.report.datamodel;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import com.hartwig.actin.ImmutablePatientRecord;
import com.hartwig.actin.PatientRecord;
import com.hartwig.actin.TestDataFactory;
import com.hartwig.actin.algo.datamodel.ImmutableTreatmentMatch;
import com.hartwig.actin.algo.datamodel.TestTreatmentMatchFactory;
import com.hartwig.actin.algo.datamodel.TreatmentMatch;

import org.junit.Test;

public class ReportFactoryTest {

    @Test
    public void canCreateReportFromTestData() {
        assertNotNull(ReportFactory.fromInputs(TestDataFactory.createMinimalTestPatientRecord(),
                TestTreatmentMatchFactory.createMinimalTreatmentMatch()));

        assertNotNull(ReportFactory.fromInputs(TestDataFactory.createProperTestPatientRecord(),
                TestTreatmentMatchFactory.createProperTreatmentMatch()));
    }

    @Test
    public void usePatientSampleIdOnMismatch() {
        PatientRecord patientRecord =
                ImmutablePatientRecord.builder().from(TestDataFactory.createMinimalTestPatientRecord()).sampleId("patient").build();

        TreatmentMatch treatmentMatch = ImmutableTreatmentMatch.builder()
                .from(TestTreatmentMatchFactory.createMinimalTreatmentMatch())
                .sampleId("treatment-match")
                .build();

        assertEquals("patient", ReportFactory.fromInputs(patientRecord, treatmentMatch).sampleId());
    }
}