package com.hartwig.actin.report.pdf.tables.trial

import com.hartwig.actin.PatientRecordFactory
import com.hartwig.actin.configuration.EnvironmentConfiguration
import com.hartwig.actin.datamodel.algo.TestTreatmentMatchFactory
import com.hartwig.actin.datamodel.clinical.TestClinicalFactory
import com.hartwig.actin.datamodel.molecular.MolecularHistory
import com.hartwig.actin.report.datamodel.Report
import com.hartwig.actin.report.datamodel.ReportFactory
import com.hartwig.actin.report.datamodel.TestReportFactory
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

private val width: Float = 1F

class EligibleApprovedTreatmentGeneratorTest {
    @Test
    fun `Should return approved treatments if available`() {
        val report = ReportFactory.fromInputs(
            PatientRecordFactory.fromInputs(TestClinicalFactory.createMinimalTestClinicalRecord(), MolecularHistory.empty()),
            TestTreatmentMatchFactory.createProperTreatmentMatch(),
            EnvironmentConfiguration.create(null)
        )
        val contents = EligibleApprovedTreatmentGenerator(report, width).contents()
        assertThat(contents.numberOfColumns == 1)
    }
}

