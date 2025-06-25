package com.hartwig.actin.report.datamodel

import com.hartwig.actin.configuration.EnvironmentConfiguration
import com.hartwig.actin.datamodel.TestPatientFactory
import com.hartwig.actin.datamodel.algo.TestTreatmentMatchFactory
import com.hartwig.actin.report.datamodel.ReportFactory.create
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class ReportFactoryTest {

    @Test
    fun `Should create report from test data`() {
        assertThat(
            create(
                TestPatientFactory.createMinimalTestWGSPatientRecord(),
                TestTreatmentMatchFactory.createMinimalTreatmentMatch(),
                EnvironmentConfiguration()
            )
        ).isNotNull()

        assertThat(
            create(
                TestPatientFactory.createProperTestPatientRecord(),
                TestTreatmentMatchFactory.createProperTreatmentMatch(),
                EnvironmentConfiguration()
            )
        ).isNotNull()
    }

    @Test
    fun `Should use patient record patient ID on mismatch`() {
        val patient = TestPatientFactory.createMinimalTestWGSPatientRecord().copy(patientId = "clinical")
        val treatmentMatch = TestTreatmentMatchFactory.createMinimalTreatmentMatch().copy(patientId = "treatment-match")

        assertThat(
            create(
                patient,
                treatmentMatch,
                EnvironmentConfiguration()
            ).patientId
        ).isEqualTo("clinical")
    }
}