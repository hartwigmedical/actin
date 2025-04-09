package com.hartwig.actin.report.pdf.util

import com.itextpdf.layout.borders.Border
import com.itextpdf.layout.element.Table
import com.itextpdf.layout.properties.UnitValue

object Tables {

    fun createFixedWidthCols(vararg widths: Float): Table {
        return Table(UnitValue.createPointArray(widths))
    }

    fun createSingleColWithWidth(width: Float): Table {
        return Table(UnitValue.createPercentArray(floatArrayOf(1f))).setWidth(width)
    }

    @JvmOverloads
    fun makeWrapping(table: Table, printSubNotes: Boolean = true): Table {
        if (table.numberOfRows == 0) {
            table.addCell(Cells.createSpanningNoneEntry(table))
        }
        table.addFooterCell(
            Cells.createSpanningSubNote(if (printSubNotes) "The table continues on the next page" else "", table)
                .setPaddingTop(5f)
                .setPaddingBottom(5f)
        )
        table.isSkipLastFooter = true

        val wrappingTable = Table(1).setMinWidth(table.width)
        if (printSubNotes) {
            wrappingTable.addHeaderCell(Cells.createSubNote("Continued from the previous page"))
        }
        wrappingTable.setSkipFirstHeader(true).addCell(Cells.create(table).setPadding(0f))

        val finalTable = Table(1).setMinWidth(table.width).setMarginBottom(20f)
        finalTable.addCell(Cells.create(wrappingTable).setPadding(0f).setBorder(Border.NO_BORDER))
        return finalTable
        
    }
}