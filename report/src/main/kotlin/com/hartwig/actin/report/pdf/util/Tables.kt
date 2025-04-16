package com.hartwig.actin.report.pdf.util

import com.itextpdf.layout.element.Table
import com.itextpdf.layout.properties.UnitValue

object Tables {

    fun createFixedWidthCols(vararg widths: Float): Table {
        return Table(UnitValue.createPointArray(widths))
    }

    fun createRelativeWidthCols(vararg widths: Float): Table {
        return Table(UnitValue.createPercentArray(widths))
    }

    fun createMultiCol(numColumns: Int): Table {
        return Table(UnitValue.createPercentArray(FloatArray(numColumns) { 1f / numColumns }))
    }

    fun createSingleCol(): Table {
        return createMultiCol(1)
    }
    
    fun createSingleColWithWidth(width: Float): Table {
        return createSingleCol().setWidth(width)
    }

    fun makeWrapping(contentTable: Table, forceKeepTogether: Boolean): Table {
        if (contentTable.numberOfRows == 0) {
            contentTable.addCell(Cells.createSpanningNoneEntry(contentTable))
        }

        if (contentTable.numberOfRows < 3 || forceKeepTogether) {
            contentTable.setKeepTogether(true)
            return contentTable
        } else {
            contentTable.addFooterCell(
                Cells.createSpanningSubNote("The table continues on the next page", contentTable)
                    .setPaddingTop(5f)
                    .setPaddingBottom(5f)
            ).setSkipLastFooter(true)

            val wrappingTable = Table(1).setWidth(contentTable.width).setMarginBottom(10f)
            wrappingTable.addHeaderCell(Cells.createSubNote("Continued from the previous page"))
            wrappingTable.setSkipFirstHeader(true).addCell(Cells.create(contentTable).setPadding(0f))

            return wrappingTable
        }
    }
}