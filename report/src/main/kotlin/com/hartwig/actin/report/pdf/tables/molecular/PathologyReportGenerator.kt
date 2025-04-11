package com.hartwig.actin.report.pdf.tables.molecular

import com.hartwig.actin.datamodel.clinical.TumorDetails
import com.hartwig.actin.report.pdf.tables.TableGenerator
import com.hartwig.actin.report.pdf.util.Styles
import com.hartwig.actin.report.pdf.util.Tables
import com.itextpdf.layout.element.Table

class PathologyReportGenerator(private val tumorDetails: TumorDetails) : TableGenerator {

    override fun title(): String {
        return "Raw pathology report"
    }

    override fun forceKeepTogether(): Boolean {
        return true
    }

    override fun contents(): Table {
        val table = Tables.createSingleCol()
        val text = tumorDetails.rawPathologyReport
        table.addCell(text).addStyle(Styles.tableContentStyle())
        return table
    }
}