package com.hartwig.actin.report.datamodel

import com.hartwig.actin.datamodel.TestPatientFactory
import com.hartwig.actin.datamodel.algo.TestTreatmentMatchFactory
import java.time.LocalDate
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class ReportFactoryTest {

    private val reportDate = LocalDate.of(2025, 7, 1)

    @Test
    fun `Should create report from test data`() {
        assertThat(
            ReportFactory.create(
                reportDate,
                TestPatientFactory.createMinimalTestWGSPatientRecord(),
                TestTreatmentMatchFactory.createMinimalTreatmentMatch()
            )
        ).isNotNull()

        assertThat(
            ReportFactory.create(
                reportDate,
                TestPatientFactory.createProperTestPatientRecord(),
                TestTreatmentMatchFactory.createProperTreatmentMatch()
            )
        ).isNotNull()
    }

    @Test
    fun `Should use patient record patient ID on mismatch`() {
        val patient = TestPatientFactory.createMinimalTestWGSPatientRecord().copy(patientId = "clinical")
        val treatmentMatch = TestTreatmentMatchFactory.createMinimalTreatmentMatch().copy(patientId = "treatment-match")

        assertThat(ReportFactory.create(reportDate, patient, treatmentMatch).patientId).isEqualTo("clinical")
    }
}