package com.hartwig.actin.report.datamodel

import com.hartwig.actin.algo.datamodel.ImmutableTreatmentMatch
import com.hartwig.actin.algo.datamodel.TestTreatmentMatchFactory
import com.hartwig.actin.algo.datamodel.TreatmentMatch
import com.hartwig.actin.clinical.datamodel.ClinicalRecord
import com.hartwig.actin.clinical.datamodel.ImmutableClinicalRecord
import com.hartwig.actin.clinical.datamodel.TestClinicalFactory
import com.hartwig.actin.molecular.datamodel.TestMolecularFactory
import com.hartwig.actin.report.datamodel.ReportFactory.fromInputs
import org.junit.Assert
import org.junit.Test

class ReportFactoryTest {
    @Test
    fun canCreateReportFromTestData() {
        Assert.assertNotNull(
            fromInputs(
                TestClinicalFactory.createMinimalTestClinicalRecord(),
                TestMolecularFactory.createMinimalTestMolecularRecord(),
                TestTreatmentMatchFactory.createMinimalTreatmentMatch()
            )
        )
        Assert.assertNotNull(
            fromInputs(
                TestClinicalFactory.createProperTestClinicalRecord(),
                TestMolecularFactory.createProperTestMolecularRecord(),
                TestTreatmentMatchFactory.createProperTreatmentMatch()
            )
        )
    }

    @Test
    fun useClinicalPatientIdOnMismatch() {
        val clinical: ClinicalRecord = ImmutableClinicalRecord.builder()
            .from(TestClinicalFactory.createMinimalTestClinicalRecord())
            .patientId("clinical")
            .build()
        val molecular = TestMolecularFactory.createMinimalTestMolecularRecord()
        val treatmentMatch: TreatmentMatch = ImmutableTreatmentMatch.builder()
            .from(TestTreatmentMatchFactory.createMinimalTreatmentMatch())
            .patientId("treatment-match")
            .build()
        Assert.assertEquals("clinical", fromInputs(clinical, molecular, treatmentMatch).patientId())
    }
}