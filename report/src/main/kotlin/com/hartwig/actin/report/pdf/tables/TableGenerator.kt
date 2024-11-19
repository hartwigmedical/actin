package com.hartwig.actin.report.pdf.tables

import com.itextpdf.layout.element.Table

interface TableGenerator {

    fun title(): String

    fun contents(): Table
}