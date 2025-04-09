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
    fun makeWrapping(contentTable: Table): Table {
        if (contentTable.numberOfRows == 0) {
            contentTable.addCell(Cells.createSpanningNoneEntry(contentTable))
        }

        contentTable.addFooterCell(
            Cells.createSpanningSubNote("The table continues on the next page", contentTable)
                .setPaddingTop(5F)
                .setPaddingBottom(5F)
        ).setSkipLastFooter(true)

        val continuedWrappingTable = Table(1).setMinWidth(contentTable.width)
        continuedWrappingTable.addHeaderCell(Cells.createSubNote("Continued from the previous page"))
        continuedWrappingTable.setSkipFirstHeader(true).addCell(Cells.create(contentTable).setPadding(0F))

        val wrappedTable = Table(1).setMinWidth(contentTable.width).setMarginBottom(10F)
        wrappedTable.addCell(Cells.create(continuedWrappingTable).setPadding(0F).setBorder(Border.NO_BORDER))
        return wrappedTable
    }
}