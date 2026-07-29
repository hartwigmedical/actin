package com.hartwig.actin.report.pdf.chapters

import com.hartwig.actin.configuration.ReportConfiguration
import com.hartwig.actin.configuration.ReportIntendedUse
import com.hartwig.actin.report.datamodel.TestReportFactory
import com.hartwig.actin.report.pdf.ReportLabels
import com.hartwig.actin.report.pdf.tables.clinical.BloodTransfusionGenerator
import com.hartwig.actin.report.pdf.tables.clinical.ClinicalSummaryGenerator
import com.hartwig.actin.report.pdf.tables.clinical.MedicationGenerator
import com.hartwig.actin.report.pdf.tables.clinical.PatientCurrentDetailsGenerator
import com.hartwig.actin.report.pdf.tables.clinical.TumorDetailsGenerator
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class ClinicalDetailsChapterTest {

    private val proper = TestReportFactory.createProperTestReport()
    private val configuration = ReportConfiguration()
    private val labels = ReportLabels.load(ReportIntendedUse.RESEARCH_USE_ONLY)

    @Test
    fun `Should provide all clinical details tables when details are provided`() {
        val tables = ClinicalDetailsChapter(proper, configuration, labels).createClinicalDetailGenerators()

        assertThat(tables.map { it::class }).containsExactly(
            ClinicalSummaryGenerator::class,
            PatientCurrentDetailsGenerator::class,
            TumorDetailsGenerator::class,
            MedicationGenerator::class,
            BloodTransfusionGenerator::class
        )
    }

    @Test
    fun `Should omit medication and bloodTransfusion tables when data not provided`() {
        val report = proper.copy(
            patientRecord = proper.patientRecord.copy(
                medications = null,
                bloodTransfusions = emptyList()
            )
        )
        val tables = ClinicalDetailsChapter(report, configuration, labels).createClinicalDetailGenerators()

        assertThat(tables.map { it::class }).containsExactly(
            ClinicalSummaryGenerator::class,
            PatientCurrentDetailsGenerator::class,
            TumorDetailsGenerator::class
        )
    }
}