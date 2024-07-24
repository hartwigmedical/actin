package com.hartwig.actin.report.pdf

import com.hartwig.actin.report.pdf.tables.TableGenerator
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Table
import com.itextpdf.layout.element.Text
import org.assertj.core.api.Assertions

fun getWrappedTable(result: TableGenerator) = result.contents().getCell(0, 0).children[0] as Table

fun getCellContents(table: Table, row: Int, column: Int): String =
    ((table.getCell(row, column).children[0] as Paragraph).children[0] as Text).text

fun assertRow(contentTable: Table, row: Int, vararg columns: String) {
    for ((index, column) in columns.withIndex()) {
        Assertions.assertThat(getCellContents(contentTable, row, index)).isEqualTo(column)
    }
}
