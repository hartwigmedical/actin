package com.hartwig.actin.report.pdf.chapters

import com.hartwig.actin.report.pdf.util.Constants
import com.hartwig.actin.report.pdf.util.Styles
import com.itextpdf.kernel.geom.PageSize
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Paragraph

interface ReportChapter {

    fun name(): String

    fun pageSize(): PageSize

    fun include() : Boolean

    fun render(document: Document)

    fun contentHeight(): Float {
        return pageSize().height - (5 + Constants.PAGE_MARGIN_TOP + Constants.PAGE_MARGIN_BOTTOM)
    }
    
    fun contentWidth(): Float {
        return pageSize().width - (5 + Constants.PAGE_MARGIN_LEFT + Constants.PAGE_MARGIN_RIGHT)
    }

    fun addChapterTitle(document: Document) {
        document.add(Paragraph(name()).addStyle(Styles.chapterTitleStyle()))
    }
}