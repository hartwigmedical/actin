package com.hartwig.actin.report.pdf.tables.molecular

import com.hartwig.actin.molecular.datamodel.ExperimentType
import com.hartwig.actin.molecular.datamodel.GeneAlteration
import com.hartwig.actin.molecular.datamodel.MolecularHistory
import com.hartwig.actin.molecular.datamodel.MolecularTest
import com.hartwig.actin.report.pdf.tables.TableGenerator
import com.hartwig.actin.report.pdf.util.Cells
import com.hartwig.actin.report.pdf.util.Tables
import com.hartwig.actin.report.pdf.util.Tables.makeWrapping
import com.itextpdf.layout.element.Table

class LongitudinalMolecularHistoryGenerator(private val molecularHistory: MolecularHistory, private val width: Float) : TableGenerator {

    override fun title(): String {
        return "Molecular history"
    }

    override fun contents(): Table {
        val sortedAndFilteredTests = molecularHistory.molecularTests.filter { it.experimentType != ExperimentType.IHC }.sortedBy { it.date }
        val driverSet =
            molecularHistory.molecularTests.map {
                it to (it.drivers.variants + it.drivers.fusions +
                        it.drivers.viruses + it.drivers.copyNumbers + it.drivers.disruptions)
            }

        val testByDriver = driverSet.flatMap { it.second.map { d -> d to it } }.groupBy { it.first.event }
            .mapValues { it.value.map { v -> v.second.first } }

        val allDrivers = driverSet.flatMap { it.second }.toSet()
        val columnCount = 3 + sortedAndFilteredTests.size
        val columnWidth = width / columnCount
        val table = Tables.createFixedWidthCols(*IntRange(1, columnCount).map { columnWidth }.toFloatArray())

        table.addHeaderCell(Cells.createHeader("Mutation"))
        table.addHeaderCell(Cells.createHeader("Interpretation"))
        table.addHeaderCell(Cells.createHeader("Driver likelihood"))

        for (test in sortedAndFilteredTests) {
            table.addHeaderCell(Cells.createHeader(testDisplay(test)))
        }

        for (driver in allDrivers) {
            table.addCell(Cells.createContent(driver.event))
            table.addCell(Cells.createContent(LongitudinalVariantInterpretation.interpret(driver as GeneAlteration)))
            table.addCell(Cells.createContent(driver.driverLikelihood.toString()))
            for (test in sortedAndFilteredTests) {
                if (testByDriver[driver.event]?.contains(test) == true) {
                    table.addCell(Cells.createContent("Detected"))
                } else {
                    table.addCell(Cells.createContent("Not detected"))
                }
            }
        }
        table.addCell(Cells.createContent("TMB"))
        table.addCell(Cells.createContent(""))
        table.addCell(Cells.createContent(""))
        for (test in sortedAndFilteredTests) {
            table.addCell(Cells.createContent(test.characteristics.tumorMutationalBurden?.toString() ?: ""))
        }
        table.addCell(Cells.createContent("MSI"))
        table.addCell(Cells.createContent(""))
        table.addCell(Cells.createContent(""))
        for (test in sortedAndFilteredTests) {
            table.addCell(Cells.createContent(if (test.characteristics.isMicrosatelliteUnstable == false) "Stable" else "Unstable"))
        }
        return makeWrapping(table)
    }

    private fun testDisplay(test: MolecularTest): String {
        return "${test.date}\n ${test.testTypeDisplay ?: test.experimentType.display()}"
    }
}