package com.hartwig.actin.report.pdf.tables.clinical

import com.itextpdf.layout.element.Cell
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Text

object CellTestUtil {

    fun extractTextFromCell(cell: Cell): String? {
        val paragraph = cell.children.firstOrNull() as? Paragraph
        val textElements = paragraph?.children?.filterIsInstance<Text>()
        return textElements?.joinToString("") { it.text }
    }
}