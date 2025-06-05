package com.hartwig.actin.report.pdf.tables.clinical

import com.hartwig.actin.datamodel.clinical.BloodTransfusion
import com.hartwig.actin.report.pdf.tables.TableGenerator
import com.hartwig.actin.report.pdf.util.Cells
import com.hartwig.actin.report.pdf.util.Formats.date
import com.hartwig.actin.report.pdf.util.Tables
import com.itextpdf.layout.element.Table

class BloodTransfusionGenerator(private val bloodTransfusions: List<BloodTransfusion>) : TableGenerator {
    
    override fun title(): String {
        return "Blood transfusions"
    }

    override fun forceKeepTogether(): Boolean {
        return false
    }
    
    override fun contents(): Table {
        val table = Tables.createRelativeWidthCols(1f, 1f, 1f)
        table.addHeaderCell(Cells.createHeader("Product"))
        table.addHeaderCell(Cells.createHeader("Date"))
        table.addHeaderCell(Cells.createHeader(""))

        for (bloodTransfusion in bloodTransfusions) {
            table.addCell(Cells.createContent(bloodTransfusion.product))
            table.addCell(Cells.createContent(date(bloodTransfusion.date)))
            table.addCell(Cells.createContent(""))
        }
        return table
    }
}