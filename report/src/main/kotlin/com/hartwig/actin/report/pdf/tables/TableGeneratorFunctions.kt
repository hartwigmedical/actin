package com.hartwig.actin.report.pdf.tables

import com.hartwig.actin.report.pdf.util.Cells
import com.hartwig.actin.report.pdf.util.Formats
import com.hartwig.actin.report.pdf.util.Tables
import com.itextpdf.layout.element.Table

object TableGeneratorFunctions {

    fun addGenerators(
        generators: List<TableGenerator>,
        table: Table,
        overrideTitleFormatToSubtitle: Boolean,
        skipWrappingFooter: Boolean = false
    ) {
        val outerTableWidth = table.width.value - Formats.STANDARD_INNER_TABLE_WIDTH_DECREASE
        val innerTableWidth = table.width.value - 2 * Formats.STANDARD_INNER_TABLE_WIDTH_DECREASE

        generators.map { generator ->
            val generatedTable = Tables.createSingleColWithWidth(outerTableWidth).setFixedLayout()
            if (overrideTitleFormatToSubtitle) {
                generatedTable.addCell(Cells.createSubTitle(generator.title()))
            } else {
                generatedTable.addCell(Cells.createTitle(generator.title()))
            }
            val contentTable = generator.contents().setWidth(innerTableWidth).setFixedLayout()

            // KD: Note, in order to more accurately test the layout of the tables, it helps to set the border of the both the generated
            // table and the main table (e.g. remove Cells.create from below line and the map output
            generatedTable.addCell(Cells.create(Tables.makeWrapping(contentTable, generator.forceKeepTogether(), skipWrappingFooter)))
            if (contentTable.numberOfRows < 3 || generator.forceKeepTogether()) {
                generatedTable.setKeepTogether(true)
            }
            Cells.create(generatedTable)
        }.forEach(table::addCell)
    }
}