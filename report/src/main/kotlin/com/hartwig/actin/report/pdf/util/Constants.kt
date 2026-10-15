package com.hartwig.actin.report.pdf.util

object Constants {

    const val METADATA_AUTHOR = "Hartwig Medical Foundation"

    const val XMP_NAMESPACE_URI = "http://actin.hartwig.com/xmp/1.0/"
    const val XMP_NAMESPACE_PREFIX = "actin"
    const val XMP_PROPERTY_MAY_BE_SHARED = "MayBeShared"

    const val PAGE_MARGIN_TOP = 100f // Top margin also excludes the chapter title, which is rendered in the header
    const val PAGE_MARGIN_LEFT = 30f
    const val PAGE_MARGIN_RIGHT = 30f
    const val PAGE_MARGIN_BOTTOM = 48f

    const val SIDE_PANEL_WIDTH = 170f
}