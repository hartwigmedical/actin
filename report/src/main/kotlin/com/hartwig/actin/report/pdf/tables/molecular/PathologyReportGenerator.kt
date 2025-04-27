package com.hartwig.actin.report.pdf.tables.molecular

import com.hartwig.actin.datamodel.clinical.PathologyReport
import com.hartwig.actin.report.pdf.tables.TableGenerator
import com.hartwig.actin.report.pdf.util.Cells
import com.hartwig.actin.report.pdf.util.Styles
import com.hartwig.actin.report.pdf.util.Tables
import com.itextpdf.layout.element.Table

class PathologyReportGenerator(private val pathologyReports: List<PathologyReport>?) : TableGenerator {

    override fun title(): String {
        return "Input pathology report"
    }

    override fun forceKeepTogether(): Boolean {
        return false
    }

    override fun contents(): Table {
        val table = Tables.createSingleCol()
        pathologyReports
            ?.mapNotNull { report -> report.report.takeIf { it.isNotBlank() } }
            ?.forEach {
                table.addCell(Cells.create(Tables.createSingleCol().addCell(it).addStyle(Styles.tableContentStyle())))
            }
        return table
    }
}