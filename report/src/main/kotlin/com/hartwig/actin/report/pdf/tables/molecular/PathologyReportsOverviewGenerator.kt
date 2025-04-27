package com.hartwig.actin.report.pdf.tables.molecular

import com.hartwig.actin.datamodel.clinical.PathologyReport
import com.hartwig.actin.report.pdf.tables.TableGenerator
import com.hartwig.actin.report.pdf.util.Cells
import com.hartwig.actin.report.pdf.util.Formats.date
import com.hartwig.actin.report.pdf.util.Styles
import com.hartwig.actin.report.pdf.util.Tables
import com.itextpdf.layout.element.Cell
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Table
import com.itextpdf.layout.element.Text

class PathologyReportsOverviewGenerator(private val pathologyReports: List<PathologyReport>?) : TableGenerator {

    override fun title(): String {
        return "Tissue samples collected"
    }

    override fun forceKeepTogether(): Boolean {
        return false
    }

    override fun contents(): Table {
        val table = Tables.createSingleCol()
        pathologyReports
            ?.filter { !it.tissueId.isNullOrBlank() }
            ?.forEach { table.addCell(createCell(it)) }
        return table
    }

    private fun createCell(report: PathologyReport): Cell {
        return Cells.create(
            Paragraph().add(Text(report.tissueId?.uppercase()).addStyle(Styles.tableHighlightStyle())).add(Text(" (Collection date: "))
                .add(Text(date(report.tissueDate)).addStyle(Styles.tableHighlightStyle())).add(Text(", Authorization date: "))
                .add(Text(date(report.authorisationDate)).addStyle(Styles.tableHighlightStyle())).add(Text(", Diagnosis: "))
                .add(Text(report.diagnosis).addStyle(Styles.tableHighlightStyle())).add(Text(")"))
        ).addStyle(Styles.tableContentStyle())
    }
}