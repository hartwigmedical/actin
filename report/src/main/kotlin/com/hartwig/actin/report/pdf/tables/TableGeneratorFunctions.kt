package com.hartwig.actin.report.pdf.tables

import com.hartwig.actin.report.pdf.util.Cells
import com.hartwig.actin.report.pdf.util.Tables
import com.itextpdf.layout.element.Table

object TableGeneratorFunctions {

    fun addGenerators(generators: List<TableGenerator>, table: Table, overrideTitleFormatToSubtitle: Boolean) {
        generators.map { generator ->
            val generatedTable = Table(1).setWidth(table.width.value - 5)
            if (overrideTitleFormatToSubtitle) {
                generatedTable.addCell(Cells.createSubTitle(generator.title()))
            } else {
                generatedTable.addCell(Cells.createTitle(generator.title()))
            }
            val contentTable = generator.contents()

            generatedTable.addCell(Cells.create(Tables.makeWrapping(contentTable, generator.forceKeepTogether())))
            if (contentTable.numberOfRows < 3) {
                generatedTable.setKeepTogether(true)
            }
            Cells.create(generatedTable)
        }.forEach(table::addCell)
    }
}