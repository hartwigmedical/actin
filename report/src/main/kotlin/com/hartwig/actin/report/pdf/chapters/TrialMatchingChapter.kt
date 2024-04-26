package com.hartwig.actin.report.pdf.chapters

import com.hartwig.actin.report.datamodel.Report
import com.hartwig.actin.report.interpretation.EvaluatedCohortFactory
import com.hartwig.actin.report.pdf.tables.trial.EligibleActinTrialsGenerator
import com.hartwig.actin.report.pdf.tables.trial.IneligibleActinTrialsGenerator
import com.hartwig.actin.report.pdf.util.Cells
import com.hartwig.actin.report.pdf.util.Styles
import com.hartwig.actin.report.pdf.util.Tables
import com.itextpdf.kernel.geom.PageSize
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Paragraph

class TrialMatchingChapter(private val report: Report, private val enableExtendedMode: Boolean) : ReportChapter {
    override fun name(): String {
        return "Trial Matching Summary"
    }

    override fun pageSize(): PageSize {
        return PageSize.A4
    }

    override fun render(document: Document) {
        addChapterTitle(document)
        addTrialMatchingOverview(document)
    }

    private fun addTrialMatchingOverview(document: Document) {
        val table = Tables.createSingleColWithWidth(contentWidth())
        val cohorts = EvaluatedCohortFactory.create(report.treatmentMatch)
        val generators = listOf(
            EligibleActinTrialsGenerator.forClosedCohorts(cohorts, report.treatmentMatch.trialSource, contentWidth(), enableExtendedMode),
            IneligibleActinTrialsGenerator.fromEvaluatedCohorts(
                cohorts,
                report.treatmentMatch.trialSource,
                contentWidth(),
                enableExtendedMode
            )
        )

        for (i in generators.indices) {
            val generator = generators[i]
            table.addCell(Cells.createTitle(generator.title()))
            table.addCell(Cells.create(generator.contents()))
            if (i < generators.size - 1) {
                table.addCell(Cells.createEmpty())
            }
        }
        document.add(table)
    }
}