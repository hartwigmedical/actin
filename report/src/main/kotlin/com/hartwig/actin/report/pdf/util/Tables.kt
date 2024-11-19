package com.hartwig.actin.report.pdf.util

import com.itextpdf.layout.element.Cell
import com.itextpdf.layout.element.Table
import com.itextpdf.layout.properties.UnitValue
import com.itextpdf.layout.properties.VerticalAlignment

object Tables {
    fun createFixedWidthCols(vararg widths: Float): Table {
        return Table(UnitValue.createPointArray(widths))
    }

    fun createSingleColWithWidth(width: Float): Table {
        return Table(UnitValue.createPercentArray(floatArrayOf(1f))).setWidth(width)
    }

    fun makeWrapping(table: Table, printSubNotes: Boolean = true): Table {
        if (table.numberOfRows == 0) {
            table.addCell(Cells.createSpanningNoneEntry(table))
        }
        if (printSubNotes) {
            table.addFooterCell(
                Cells.createSpanningSubNote("The table continues on the next page", table).setVerticalAlignment(VerticalAlignment.BOTTOM)
            )
        }
        table.isSkipLastFooter = true

        val wrappingTable = Table(1).setMinWidth(table.width)

        table.children.filterIsInstance<Cell>().forEach { it.setKeepTogether(true) }

        if (printSubNotes) {
            wrappingTable.addHeaderCell(Cells.createSubNote("Continued from the previous page"))
        }

        wrappingTable.setSkipFirstHeader(true).addCell(Cells.create(table).setPadding(0f))
        return wrappingTable
    }
}