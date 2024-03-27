package com.hartwig.actin.report.datamodel

import com.hartwig.actin.TestPatientFactory
import com.hartwig.actin.algo.datamodel.TestTreatmentMatchFactory
import com.hartwig.actin.clinical.datamodel.TestClinicalFactory
import com.hartwig.actin.molecular.datamodel.TestMolecularFactory
import com.hartwig.actin.report.ReportConfiguration
import com.hartwig.actin.report.datamodel.ReportFactory.fromInputs
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class ReportFactoryTest {
    @Test
    fun `Should create report from test data`() {
        assertThat(
            fromInputs(
                TestPatientFactory.createMinimalTestPatientRecord(),
                TestTreatmentMatchFactory.createMinimalTreatmentMatch(),
                ReportConfiguration()
            )
        ).isNotNull
        assertThat(
            fromInputs(
                TestPatientFactory.createProperTestPatientRecord(),
                TestTreatmentMatchFactory.createProperTreatmentMatch(),
                ReportConfiguration()
            )
        ).isNotNull
    }

    @Test
    fun `Should use clinical patient ID on mismatch`() {
        val patient = TestPatientFactory.createMinimalTestPatientRecord().copy(patientId = "clinical")
        val treatmentMatch = TestTreatmentMatchFactory.createMinimalTreatmentMatch().copy(patientId = "treatment-match")
        assertThat(fromInputs(patient, treatmentMatch, ReportConfiguration()).patientId).isEqualTo("clinical")
    }
}