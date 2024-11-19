package com.hartwig.actin.report.pdf.chapters

import com.hartwig.actin.report.pdf.util.Constants
import com.hartwig.actin.report.pdf.util.Styles
import com.itextpdf.kernel.geom.PageSize
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Paragraph

interface ReportChapter {

    fun name(): String

    fun pageSize(): PageSize

    fun contentWidth(): Float {
        return pageSize().width - (5 + Constants.PAGE_MARGIN_LEFT + Constants.PAGE_MARGIN_RIGHT)
    }

    fun addChapterTitle(document: Document) {
        document.add(Paragraph(name()).addStyle(Styles.chapterTitleStyle()))
    }

    fun render(document: Document)

    val include: Boolean
        get() = true
}