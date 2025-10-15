package com.hartwig.actin.report.pdf

import com.hartwig.actin.report.datamodel.TestReportFactory
import com.hartwig.actin.report.pdf.chapters.ClinicalDetailsChapter
import com.hartwig.actin.report.pdf.chapters.MolecularDetailsChapter
import com.hartwig.actin.report.pdf.chapters.SummaryChapter
import com.hartwig.actin.report.pdf.chapters.TrialMatchingDetailsChapter
import com.hartwig.actin.report.pdf.chapters.TrialMatchingOtherResultsChapter
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

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
}