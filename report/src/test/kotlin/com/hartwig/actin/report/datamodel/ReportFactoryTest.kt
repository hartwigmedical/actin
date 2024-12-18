package com.hartwig.actin.report.datamodel

import com.hartwig.actin.configuration.EnvironmentConfiguration
import com.hartwig.actin.datamodel.TestPatientFactory
import com.hartwig.actin.datamodel.algo.TestTreatmentMatchFactory
import com.hartwig.actin.report.datamodel.ReportFactory.fromInputs
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class ReportFactoryTest {

    @Test
    fun `Should create report from test data`() {
        assertThat(
            fromInputs(
                TestPatientFactory.createMinimalTestWGSPatientRecord(),
                TestTreatmentMatchFactory.createMinimalTreatmentMatch(),
                EnvironmentConfiguration(requestingHospital = "NKI-AvL")
            )
        ).isNotNull

        assertThat(
            fromInputs(
                TestPatientFactory.createProperTestPatientRecord(),
                TestTreatmentMatchFactory.createProperTreatmentMatch(),
                EnvironmentConfiguration(requestingHospital = "NKI-AvL")
            )
        ).isNotNull
    }

    @Test
    fun `Should use clinical patient ID on mismatch`() {
        val patient = TestPatientFactory.createMinimalTestWGSPatientRecord().copy(patientId = "clinical")
        val treatmentMatch = TestTreatmentMatchFactory.createMinimalTreatmentMatch().copy(patientId = "treatment-match")

        assertThat(
            fromInputs(
                patient,
                treatmentMatch,
                EnvironmentConfiguration(requestingHospital = "NKI-AvL")
            ).patientId
        ).isEqualTo("clinical")
    }
}