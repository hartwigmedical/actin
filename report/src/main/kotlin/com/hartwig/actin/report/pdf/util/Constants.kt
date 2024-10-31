package com.hartwig.actin.report.pdf.util

import com.hartwig.actin.report.ReporterApplication

object Constants {
    val METADATA_TITLE = "Hartwig ACTIN Report v${ReporterApplication.VERSION}"
    const val METADATA_AUTHOR = "Hartwig ACTIN System"
    const val PAGE_MARGIN_TOP = 100f // Top margin also excludes the chapter title, which is rendered in the header
    const val PAGE_MARGIN_LEFT = 30f
    const val PAGE_MARGIN_RIGHT = 30f
    const val PAGE_MARGIN_BOTTOM = 40f
}