package com.hartwig.actin.report.pdf.chapters

import com.hartwig.actin.report.pdf.tables.TableGenerator
import com.hartwig.actin.report.pdf.util.Cells
import com.hartwig.actin.report.pdf.util.Tables
import com.itextpdf.layout.element.Table

object ChapterContentFunctions {

    fun addGenerators(generators: List<TableGenerator>, table: Table, overrideTitleFormatToSubtitle: Boolean) {
        generators.map { generator ->
            val generatedTable = Table(1).setMinWidth(table.width)
            if (overrideTitleFormatToSubtitle) {
                generatedTable.addCell(Cells.createSubTitle(generator.title()))
            } else {
                generatedTable.addCell(Cells.createTitle(generator.title()))
            }
            val contentTable = generator.contents()
            generatedTable.addCell(Tables.makeWrapping(contentTable))
            if (contentTable.numberOfRows < 3) {
                contentTable.setKeepTogether(true)
                generatedTable.setKeepTogether(true)
            }
            Cells.createContent(generatedTable)
        }.forEach(table::addCell)
    }
}