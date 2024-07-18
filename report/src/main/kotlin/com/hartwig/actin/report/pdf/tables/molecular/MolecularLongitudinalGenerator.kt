package com.hartwig.actin.report.pdf.tables.molecular

import com.hartwig.actin.molecular.datamodel.Driver
import com.hartwig.actin.molecular.datamodel.ExperimentType
import com.hartwig.actin.molecular.datamodel.MolecularHistory
import com.hartwig.actin.molecular.datamodel.MolecularTest
import com.hartwig.actin.molecular.datamodel.Variant
import com.hartwig.actin.molecular.datamodel.orange.driver.CopyNumber
import com.hartwig.actin.report.pdf.tables.TableGenerator
import com.hartwig.actin.report.pdf.util.Cells
import com.hartwig.actin.report.pdf.util.Tables
import com.hartwig.actin.report.pdf.util.Tables.makeWrapping
import com.itextpdf.layout.element.Table

class MolecularLongitudinalGenerator(private val molecularHistory: MolecularHistory, private val width: Float) : TableGenerator {
    override fun title(): String {
        return "Molecular history"
    }

    private fun typeDisplay(driver: Driver): String {
        return when (driver) {
            is Variant -> driver.canonicalImpact.codingEffect?.display() ?: ""
            is CopyNumber -> if (driver.type.isGain) "Amplification" else "Deletion"
            else -> ""
        }
    }

    override fun contents(): Table {
        val sortedAndFilteredTests = molecularHistory.molecularTests.filter { it.experimentType != ExperimentType.IHC }.sortedBy { it.date }
        val driverSet =
            molecularHistory.molecularTests.map { it to (it.drivers.variants + it.drivers.fusions + it.drivers.viruses + it.drivers.copyNumbers + it.drivers.disruptions) }

        val testByDriver = driverSet.flatMap { it.second.map { d -> d to it } }.groupBy { it.first.event }
            .mapValues { it.value.map { v -> v.second.first } }

        val allDrivers = driverSet.flatMap { it.second }.toSet()
        val columnCount = 3 + sortedAndFilteredTests.size
        val columnWidth = width / columnCount
        val table = Tables.createFixedWidthCols(*IntRange(1, columnCount).map { columnWidth }.toFloatArray())

        table.addHeaderCell(Cells.createHeaderWithPadding("Event"))
        table.addHeaderCell(Cells.createHeaderWithPadding("Type"))
        table.addHeaderCell(Cells.createHeaderWithPadding("Driver likelihood"))

        for (test in sortedAndFilteredTests) {
            table.addHeaderCell(Cells.createHeaderWithPadding(testDisplay(test)))
        }

        for (driver in allDrivers) {
            table.addCell(Cells.createContentWithPadding(driver.event))
            table.addCell(Cells.createContentWithPadding(typeDisplay(driver)))
            table.addCell(Cells.createContentWithPadding(driver.driverLikelihood.toString()))
            for (test in sortedAndFilteredTests) {
                if (testByDriver[driver.event]?.contains(test) == true) {
                    table.addCell(Cells.createContentWithPadding("Detected"))
                } else {
                    table.addCell(Cells.createContentWithPadding("Not detected"))
                }
            }
        }
        table.addCell(Cells.createContentWithPadding("TMB"))
        table.addCell(Cells.createContentWithPadding(""))
        table.addCell(Cells.createContentWithPadding(""))
        for (test in sortedAndFilteredTests) {
            table.addCell(Cells.createContentWithPadding(test.characteristics.tumorMutationalBurden?.toString() ?: ""))
        }
        table.addCell(Cells.createContentWithPadding("MSI"))
        table.addCell(Cells.createContentWithPadding(""))
        table.addCell(Cells.createContentWithPadding(""))
        for (test in sortedAndFilteredTests) {
            table.addCell(Cells.createContentWithPadding(if (test.characteristics.isMicrosatelliteUnstable == false) "Stable" else "Unstable"))
        }
        return makeWrapping(table)
    }

    private fun testDisplay(test: MolecularTest): String {
        return "${test.date}\n ${test.testType ?: test.experimentType.display()}"
    }
}