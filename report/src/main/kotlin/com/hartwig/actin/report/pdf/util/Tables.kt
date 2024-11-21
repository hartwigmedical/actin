package com.hartwig.actin.report.pdf.util

import com.hartwig.actin.report.pdf.util.Cells.createSpanningSubNote
import com.itextpdf.layout.element.Cell
import com.itextpdf.layout.element.Table
import com.itextpdf.layout.properties.UnitValue

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

        val wrappingTable = Table(1).setMinWidth(table.width)

        table.children.filterIsInstance<Cell>().forEach { it.setKeepTogether(true).setPadding(0f) }

        if (printSubNotes) {
            wrappingTable.addFooterCell(
                createSpanningSubNote("The table continues on the next page", table).setFixedPosition(30f, 40f, 0f)
            )
            wrappingTable.addHeaderCell(Cells.createSubNote("Continued from the previous page"))
                .setSkipFirstHeader(true)
                .setSkipLastFooter(true)
        }

        wrappingTable.addCell(Cells.create(table)).setPadding(0f)
        return wrappingTable
    }
}