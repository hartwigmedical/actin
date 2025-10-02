package com.hartwig.actin.report.datamodel

import com.hartwig.actin.configuration.ReportConfiguration
import com.hartwig.actin.datamodel.TestPatientFactory
import com.hartwig.actin.datamodel.algo.TestTreatmentMatchFactory
import com.hartwig.actin.report.datamodel.ReportFactory.create
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import java.time.LocalDate

class ReportFactoryTest {

    private val reportDate = LocalDate.of(2025, 7, 1)
    
    @Test
    fun `Should create report from test data`() {
        assertThat(
            create(
                reportDate,
                TestPatientFactory.createMinimalTestWGSPatientRecord(),
                TestTreatmentMatchFactory.createMinimalTreatmentMatch(),
                ReportConfiguration()
            )
        ).isNotNull()

        assertThat(
            create(
                reportDate,
                TestPatientFactory.createProperTestPatientRecord(),
                TestTreatmentMatchFactory.createProperTreatmentMatch(),
                ReportConfiguration()
            )
        ).isNotNull()
    }

    @Test
    fun `Should use patient record patient ID on mismatch`() {
        val patient = TestPatientFactory.createMinimalTestWGSPatientRecord().copy(patientId = "clinical")
        val treatmentMatch = TestTreatmentMatchFactory.createMinimalTreatmentMatch().copy(patientId = "treatment-match")

        assertThat(create(reportDate, patient, treatmentMatch, ReportConfiguration()).patientId).isEqualTo("clinical")
    }
}