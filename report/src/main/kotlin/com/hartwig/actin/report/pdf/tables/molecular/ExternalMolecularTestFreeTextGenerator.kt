package com.hartwig.actin.report.pdf.tables.molecular

import com.hartwig.actin.molecular.datamodel.MolecularHistory
import com.hartwig.actin.report.pdf.tables.TableGenerator
import com.itextpdf.layout.element.Table

class ExternalMolecularTestFreeTextGenerator(val molecularHistory: MolecularHistory, private val width: Float) : TableGenerator {
    override fun title(): String {
        return "Unaltered result text of external molecular test(s):"
    }

    override fun contents(): Table {
        val table = Table(1).setWidth(width)
        TODO("Add actual external test result text below")
        val text = molecularHistory.molecularTests
        return table
    }


}