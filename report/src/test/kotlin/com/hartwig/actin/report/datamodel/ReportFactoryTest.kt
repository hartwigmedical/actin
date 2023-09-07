package com.hartwig.actin.report.datamodel

import com.hartwig.actin.algo.datamodel.ImmutableTreatmentMatch
import com.hartwig.actin.algo.datamodel.TestTreatmentMatchFactory
import com.hartwig.actin.algo.datamodel.TreatmentMatch
import com.hartwig.actin.clinical.datamodel.ClinicalRecord
import com.hartwig.actin.clinical.datamodel.ImmutableClinicalRecord
import com.hartwig.actin.clinical.datamodel.TestClinicalFactory
import com.hartwig.actin.molecular.datamodel.TestMolecularFactory
import com.hartwig.actin.report.datamodel.ReportFactory.fromInputs
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class ReportFactoryTest {
    @Test
    fun canCreateReportFromTestData() {
        assertThat(
            fromInputs(
                TestClinicalFactory.createMinimalTestClinicalRecord(),
                TestMolecularFactory.createMinimalTestMolecularRecord(),
                TestTreatmentMatchFactory.createMinimalTreatmentMatch()
            )
        ).isNotNull
        assertThat(
            fromInputs(
                TestClinicalFactory.createProperTestClinicalRecord(),
                TestMolecularFactory.createProperTestMolecularRecord(),
                TestTreatmentMatchFactory.createProperTreatmentMatch()
            )
        ).isNotNull
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
        assertThat(fromInputs(clinical, molecular, treatmentMatch).patientId).isEqualTo("clinical")
    }
}