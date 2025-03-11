package com.hartwig.actin.report.pdf.tables.trial

import com.hartwig.actin.PatientRecordFactory
import com.hartwig.actin.configuration.EnvironmentConfiguration
import com.hartwig.actin.datamodel.algo.TestTreatmentMatchFactory
import com.hartwig.actin.datamodel.clinical.TestClinicalFactory
import com.hartwig.actin.datamodel.molecular.MolecularHistory
import com.hartwig.actin.report.datamodel.ReportFactory
import com.hartwig.actin.report.pdf.getCellContents
import com.hartwig.actin.report.pdf.getWrappedTable
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

private val width: Float = 10F

class EligibleApprovedTreatmentGeneratorTest {
    @Test
    fun `Should return approved treatments if available`() {
        val report = ReportFactory.fromInputs(
            PatientRecordFactory.fromInputs(TestClinicalFactory.createMinimalTestClinicalRecord(), MolecularHistory.empty()),
            TestTreatmentMatchFactory.createProperTreatmentMatch(),
            EnvironmentConfiguration.create(null)
        )
        val contents = getWrappedTable(EligibleApprovedTreatmentGenerator(report, width))
        assertThat(getCellContents(contents, 0, 0)).isEqualTo("Pembrolizumab")
    }
}

