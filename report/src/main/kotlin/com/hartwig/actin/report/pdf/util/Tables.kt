package com.hartwig.actin.report.pdf.util

import com.hartwig.actin.report.pdf.tables.trial.TrialFormatFunctions
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

    fun makeWrapping(contentTable: Table, forceKeepTogether: Boolean, skipWrappingFooter: Boolean = false): Table {
        val tableOnlyContainsFootNote = if (contentTable.numberOfRows == 1) {
            val text = extractTextFromCell(contentTable.getCell(0, 0))
            TrialFormatFunctions.FOOTNOTES.any { text.contains(it) }
        } else false

        if (contentTable.numberOfRows == 0 || tableOnlyContainsFootNote) {
            val tableWithoutHeaders = createSingleColWithWidth(contentTable.width.value)
            tableWithoutHeaders.addCell(Cells.createSpanningNoneEntry(contentTable))
            for (row in 0 until contentTable.numberOfRows) {
                tableWithoutHeaders.addCell(contentTable.getCell(row, 0).clone(true))
            }
            return tableWithoutHeaders.setKeepTogether(true)
        }

        if (contentTable.numberOfRows < 3 || forceKeepTogether) {
            contentTable.isKeepTogether = true
            return contentTable
        } else {
            if (skipWrappingFooter) {
                contentTable.addFooterCell(
                    Cells.createSpanningSubNote("The table continues on the next page", contentTable)
                        .setPaddingTop(5f)
                        .setPaddingBottom(5f)
                )
            }
            contentTable.isSkipLastFooter = true

            val wrappingTable = createSingleColWithWidth(contentTable.width.value).setMarginBottom(10f)
            if (skipWrappingFooter) {
                wrappingTable.addHeaderCell(Cells.createSubNote("Continued from the previous page"))
            }
            wrappingTable.setSkipFirstHeader(true).addCell(Cells.create(contentTable).setPadding(0f))

            return wrappingTable
        }
    }
}