package com.hartwig.actin.report.datamodel

import com.hartwig.actin.algo.datamodel.TestTreatmentMatchFactory
import com.hartwig.actin.clinical.datamodel.TestClinicalFactory
import com.hartwig.actin.molecular.datamodel.TestMolecularFactory
import com.hartwig.actin.report.datamodel.ReportFactory.fromInputs
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class ReportFactoryTest {
    @Test
    fun `Should create report from test data`() {
        assertThat(
            fromInputs(
                TestClinicalFactory.createMinimalTestClinicalRecord(),
                TestMolecularFactory.createMinimalTestMolecularHistory(),
                TestTreatmentMatchFactory.createMinimalTreatmentMatch()
            )
        ).isNotNull
        assertThat(
            fromInputs(
                TestClinicalFactory.createProperTestClinicalRecord(),
                TestMolecularFactory.createProperTestMolecularHistory(),
                TestTreatmentMatchFactory.createProperTreatmentMatch()
            )
        ).isNotNull
    }

    @Test
    fun `Should use clinical patient ID on mismatch`() {
        val clinical = TestClinicalFactory.createMinimalTestClinicalRecord().copy(patientId = "clinical")
        val molecularHistorys = TestMolecularFactory.createMinimalTestMolecularHistory()
        val treatmentMatch = TestTreatmentMatchFactory.createMinimalTreatmentMatch().copy(patientId = "treatment-match")
        assertThat(fromInputs(clinical, molecularHistorys, treatmentMatch).patientId).isEqualTo("clinical")
    }
}