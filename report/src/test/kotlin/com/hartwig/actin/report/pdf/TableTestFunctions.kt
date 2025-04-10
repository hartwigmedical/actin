package com.hartwig.actin.report.pdf

import com.hartwig.actin.report.pdf.tables.TableGenerator
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Table
import com.itextpdf.layout.element.Text
import org.assertj.core.api.Assertions.assertThat

fun getCellContents(table: Table, row: Int, column: Int): String {
    return ((table.getCell(row, column).children[0] as Paragraph).children[0] as Text).text
}

fun assertHeader(generator: TableGenerator, vararg columns: String) {
    val table = generator.contents().header
    for ((index, column) in columns.withIndex()) {
        assertThat(getCellContents(table, 0, index)).isEqualTo(column)
    }
}

fun assertRow(generator: TableGenerator, row: Int, vararg columns: String) {
    val table = generator.contents()
    for ((index, column) in columns.withIndex()) {
        assertThat(getCellContents(table, row, index)).isEqualTo(column)
    }
}