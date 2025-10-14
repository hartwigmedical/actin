package com.hartwig.actin.report.pdf

import com.hartwig.actin.report.datamodel.TestReportFactory
import com.hartwig.actin.report.pdf.chapters.ClinicalDetailsChapter
import com.hartwig.actin.report.pdf.chapters.MolecularDetailsChapter
import com.hartwig.actin.report.pdf.chapters.SummaryChapter
import com.hartwig.actin.report.pdf.chapters.TrialMatchingDetailsChapter
import com.hartwig.actin.report.pdf.chapters.TrialMatchingOtherResultsChapter
import com.hartwig.actin.report.pdf.tables.clinical.BloodTransfusionGenerator
import com.hartwig.actin.report.pdf.tables.clinical.ClinicalSummaryGenerator
import com.hartwig.actin.report.pdf.tables.clinical.MedicationGenerator
import com.hartwig.actin.report.pdf.tables.clinical.PatientCurrentDetailsGenerator
import com.hartwig.actin.report.pdf.tables.clinical.TumorDetailsGenerator
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

private const val KEY_WIDTH = 100f
private const val VALUE_WIDTH = 200f

class ReportContentProviderTest {

    private val proper = TestReportFactory.createProperTestReport()
    
    @Test
    fun `Should include molecular chapter and omit efficacy chapters by default`() {
        val enableExtendedMode = false
        val chapters = ReportContentProvider(proper, enableExtendedMode).provideChapters()
        assertThat(chapters.map { it::class }).containsExactly(
            SummaryChapter::class,
            MolecularDetailsChapter::class,
            ClinicalDetailsChapter::class,
            TrialMatchingOtherResultsChapter::class,
        )
    }

    @Test
    fun `Should include trial matching details by default in extended mode`() {
        val enableExtendedMode = true
        val chapters = ReportContentProvider(proper, enableExtendedMode).provideChapters()
        assertThat(chapters.map { it::class }).containsExactly(
            SummaryChapter::class,
            MolecularDetailsChapter::class,
            ClinicalDetailsChapter::class,
            TrialMatchingOtherResultsChapter::class,
            TrialMatchingDetailsChapter::class
        )
    }

    @Test
    fun `Should provide all clinical details tables when details are provided`() {
        val tables = ReportContentProvider(proper).provideClinicalDetailsTables(KEY_WIDTH, VALUE_WIDTH)
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
        val tables = ReportContentProvider(report).provideClinicalDetailsTables(KEY_WIDTH, VALUE_WIDTH)
        assertThat(tables.map { it::class }).containsExactly(
            ClinicalSummaryGenerator::class,
            PatientCurrentDetailsGenerator::class,
            TumorDetailsGenerator::class
        )
    }
}