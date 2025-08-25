package com.hartwig.actin.report.pdf.chapters

import com.hartwig.actin.report.datamodel.Report
import com.hartwig.actin.report.interpretation.InterpretedCohort
import com.hartwig.actin.report.pdf.tables.TableGeneratorFunctions
import com.hartwig.actin.report.pdf.tables.molecular.LongitudinalMolecularHistoryGenerator
import com.hartwig.actin.report.pdf.util.Tables
import com.itextpdf.kernel.geom.PageSize
import com.itextpdf.layout.Document

class LongitudinalMolecularHistoryChapter(
    private val report: Report, private val cohorts: List<InterpretedCohort>, override val include: Boolean
) : ReportChapter {

    override fun name(): String {
        return "Molecular History"
    }

    override fun pageSize(): PageSize {
        return PageSize.A4
    }

    override fun render(document: Document) {
        addChapterTitle(document)
        addLongitudinalMolecularHistoryTable(document)
    }

    private fun addLongitudinalMolecularHistoryTable(document: Document) {
        val table = Tables.createSingleColWithWidth(contentWidth())
        val generator = LongitudinalMolecularHistoryGenerator(report.patientRecord.molecularTests, cohorts)
        TableGeneratorFunctions.addGenerators(listOf(generator), table, overrideTitleFormatToSubtitle = true)
        document.add(table)
    }
}