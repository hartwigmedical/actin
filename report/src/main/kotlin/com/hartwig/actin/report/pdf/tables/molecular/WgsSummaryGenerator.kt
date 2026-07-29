package com.hartwig.actin.report.pdf.tables.molecular

import com.hartwig.actin.datamodel.clinical.PathologyReport
import com.hartwig.actin.datamodel.molecular.MolecularHistory
import com.hartwig.actin.datamodel.molecular.MolecularTest
import com.hartwig.actin.report.interpretation.InterpretedCohort
import com.hartwig.actin.report.interpretation.MolecularDriversSummarizer
import com.hartwig.actin.report.pdf.ReportLabels
import com.hartwig.actin.report.pdf.SummaryType
import com.hartwig.actin.report.pdf.tables.TableGenerator
import com.hartwig.actin.report.pdf.util.Formats.date
import com.itextpdf.layout.element.Table

class WgsSummaryGenerator(
    private val summaryType: SummaryType,
    private val molecular: MolecularTest,
    private val pathologyReport: PathologyReport?,
    cohorts: List<InterpretedCohort>,
    private val keyWidth: Float,
    private val valueWidth: Float,
    private val labels: ReportLabels,
    private val immunologyGenerator: ImmunologyGenerator? = null
) : TableGenerator {

    private val summarizer = MolecularDriversSummarizer.fromMolecularDriversAndEvaluatedCohorts(molecular.drivers, cohorts)
    private val wgsMolecular = MolecularHistory(listOf(molecular)).latestOrangeMolecularRecord()

    override fun title(): String {
        val title = molecular.testTypeDisplay ?: molecular.experimentType.display()
        val suffix = pathologyReport?.let { "" } ?: " (${date(molecular.date)})"
        return "$title$suffix"
    }

    override fun forceKeepTogether(): Boolean {
        return true
    }

    override fun contents(): Table {
        return WgsSummaryGeneratorFunctions.createMolecularSummaryTable(
            summaryType, molecular, wgsMolecular, keyWidth, valueWidth, summarizer, labels, immunologyGenerator
        )
    }
}