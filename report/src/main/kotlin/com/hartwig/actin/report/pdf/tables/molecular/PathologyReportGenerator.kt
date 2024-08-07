package com.hartwig.actin.report.pdf.tables.molecular

import com.hartwig.actin.clinical.datamodel.TumorDetails
import com.hartwig.actin.report.pdf.tables.TableGenerator
import com.hartwig.actin.report.pdf.util.Styles
import com.itextpdf.layout.element.Table

class PathologyReportGenerator(private val tumorDetails: TumorDetails, private val width: Float) : TableGenerator {
    override fun title(): String {
        return "Raw pathology report:"
    }

    override fun contents(): Table {
        val table = Table(1).setWidth(width)
        val text = tumorDetails.rawPathologyReport
        table.addCell(text).addStyle(Styles.tableContentStyle())
        return table
    }
}