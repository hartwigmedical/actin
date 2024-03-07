package com.hartwig.actin.report.pdf.tables.molecular

import com.hartwig.actin.molecular.datamodel.MolecularRecord
import com.hartwig.actin.report.pdf.tables.TableGenerator
import com.hartwig.actin.report.pdf.util.Cells
import com.hartwig.actin.report.pdf.util.Tables
import com.itextpdf.layout.element.Table

class MolecularSummaryCRCGenerator(
    private val molecular: MolecularRecord, private val keyWidth: Float, private val valueWidth: Float
) : TableGenerator {
    override fun title(): String {
        return "Recent molecular results"
    }

    override fun contents(): Table {
        val table = Tables.createFixedWidthCols(keyWidth, valueWidth)
        table.addCell(Cells.createKey("KRAS"))
        table.addCell(Cells.createValue(geneToDrivers("KRAS")))
        table.addCell(Cells.createKey("NRAS"))
        table.addCell(Cells.createValue(geneToDrivers("NRAS")))
        table.addCell(Cells.createKey("BRAF"))
        table.addCell(Cells.createValue(geneToDrivers("NRAS")))
        table.addCell(Cells.createKey("HER2"))
        table.addCell(Cells.createValue(geneToDrivers("HER2")))
        table.addCell(Cells.createKey("Microsatellite (in)stability"))
        table.addCell(
            Cells.createValue(
                if (molecular.characteristics.isMicrosatelliteUnstable == true) {
                    "Unstable"
                } else {
                    "Stable"
                }
            )
        )

        return table
    }

    private fun geneToDrivers(geneToFind: String): String {
        return if (molecular.drivers.variants.none { it.gene == geneToFind }) {
            "Wild-type"
        } else {
            molecular.drivers.variants.filter { it.gene == geneToFind }.joinToString { it.canonicalImpact.hgvsProteinImpact }
        }
    }
}