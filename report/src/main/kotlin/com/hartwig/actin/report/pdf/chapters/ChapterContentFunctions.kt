package com.hartwig.actin.report.pdf.chapters

import com.hartwig.actin.report.pdf.tables.TableGenerator
import com.hartwig.actin.report.pdf.util.Cells
import com.itextpdf.layout.element.Table

object ChapterContentFunctions {

    fun addGenerators(generators: List<TableGenerator>, table: Table, addSubTitle: Boolean) {
        generators.flatMap { generator ->
            sequenceOf(
                if (addSubTitle) Cells.createSubTitle(generator.title()) else Cells.createTitle(generator.title()),
                Cells.create(generator.contents()),
                Cells.createEmpty()
            )
        }
            .dropLast(1)
            .forEach(table::addCell)
    }
}