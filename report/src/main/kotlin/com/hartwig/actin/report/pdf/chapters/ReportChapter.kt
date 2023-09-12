package com.hartwig.actin.report.pdf.chapters

import com.hartwig.actin.report.pdf.util.Constants
import com.itextpdf.kernel.geom.PageSize
import com.itextpdf.layout.Document

interface ReportChapter {
    fun name(): String
    fun pageSize(): PageSize
    fun contentWidth(): Float {
        return pageSize().width - (5 + Constants.PAGE_MARGIN_LEFT + Constants.PAGE_MARGIN_RIGHT)
    }

    fun render(document: Document)
}