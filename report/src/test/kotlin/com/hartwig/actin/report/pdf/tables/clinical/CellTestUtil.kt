package com.hartwig.actin.report.pdf.tables.clinical

import com.itextpdf.layout.element.Cell
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Text

object CellTestUtil {

    fun extractTextFromCell(cell: Cell): String {
        return cell.children
            .filterIsInstance<Paragraph>()
            .joinToString("\n") { paragraph ->
                paragraph.children
                    .filterIsInstance<Text>()
                    .joinToString("") { it.text }
            }
    }
}