package com.hartwig.actin.report.pdf.tables.molecular

import com.hartwig.actin.molecular.datamodel.MolecularHistory
import com.hartwig.actin.report.pdf.tables.TableGenerator
import com.hartwig.actin.report.pdf.util.Cells
import com.hartwig.actin.report.pdf.util.Tables
import com.itextpdf.layout.element.Table

class MolecularLongitudinalGenerator(private val molecularHistory: MolecularHistory, private val width: Float) : TableGenerator {
    override fun title(): String {
        return "Variant longitudinal data"
    }

    override fun contents(): Table {
        val testByDriver = molecularHistory.molecularTests.flatMap { it.drivers.variants.map { d -> d to it } }.groupBy { it.first.event }
            .mapValues { it.value.map { v -> v.second } }
        val allDrivers = molecularHistory.molecularTests.flatMap { it.drivers.variants }
        val colWidth = width / 8
        val table = Tables.createFixedWidthCols(colWidth, colWidth * 2, colWidth, colWidth, colWidth, colWidth, colWidth)

        table.addHeaderCell(Cells.createHeader("Event"))
        table.addHeaderCell(Cells.createHeader("Type"))
        table.addHeaderCell(Cells.createHeader("Driver likelihood"))
        for (test in molecularHistory.molecularTests) {
            table.addHeaderCell(Cells.createHeader(test.type.display()))
        }

        for (driver in allDrivers) {
            table.addCell(Cells.createContent(driver.event))
            table.addCell(Cells.createContent(driver.canonicalImpact.codingEffect.toString()))
            table.addCell(Cells.createContent(driver.driverLikelihood.toString()))
            for (test in molecularHistory.molecularTests) {
                if (testByDriver[driver.event]?.contains(test) == true) {
                    table.addCell(Cells.createContent(driver.driverLikelihood.toString()))
                } else {
                    table.addCell(Cells.createContent("Not detected"))
                }
            }
        }
        table.addCell(Cells.createContent("TMB"))
        table.addCell(Cells.createContent(""))
        for (test in molecularHistory.molecularTests) {
            table.addCell(Cells.createContent(test.characteristics.tumorMutationalBurden?.toString() ?: ""))
        }
        table.addCell(Cells.createContent("MSI"))
        table.addCell(Cells.createContent(""))
        for (test in molecularHistory.molecularTests) {
            table.addCell(Cells.createContent(if (test.characteristics.isMicrosatelliteUnstable == false) "Stable" else "Unstable"))
        }
        return table
    }
}