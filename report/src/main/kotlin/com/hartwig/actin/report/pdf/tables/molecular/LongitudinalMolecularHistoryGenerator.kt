package com.hartwig.actin.report.pdf.tables.molecular

import com.hartwig.actin.molecular.datamodel.ExperimentType
import com.hartwig.actin.molecular.datamodel.GeneAlteration
import com.hartwig.actin.molecular.datamodel.MolecularHistory
import com.hartwig.actin.molecular.datamodel.MolecularTest
import com.hartwig.actin.report.pdf.tables.TableGenerator
import com.hartwig.actin.report.pdf.util.Cells
import com.hartwig.actin.report.pdf.util.Tables.makeWrapping
import com.itextpdf.layout.element.Table

class LongitudinalMolecularHistoryGenerator(private val molecularHistory: MolecularHistory, private val width: Float) : TableGenerator {
    override fun title(): String {
        return "Molecular history"
    }

    override fun contents(): Table {
        val sortedAndFilteredTests = molecularHistory.molecularTests.filter { it.experimentType != ExperimentType.IHC }.sortedBy { it.date }
        val testsWithDrivers =
            molecularHistory.molecularTests.map { it to with(it.drivers) { variants + fusions + viruses + copyNumbers + disruptions } }

        val testsByDriverEvent = testsWithDrivers.flatMap { (test, drivers) -> drivers.map { d -> d.event to test } }
            .groupBy({ (event, _) -> event }, { (_, test) -> test })

        val allDrivers = testsWithDrivers.flatMap { it.second }.toSet()
        val columnCount = 3 + sortedAndFilteredTests.size
        val table = Table(columnCount).setWidth(width)

        table.addHeaderCell(Cells.createHeader("Event"))
        table.addHeaderCell(Cells.createHeader("Description"))
        table.addHeaderCell(Cells.createHeader("Driver likelihood"))

        for (test in sortedAndFilteredTests) {
            table.addHeaderCell(Cells.createHeader(testDisplay(test)))
        }

        for (driver in allDrivers) {
            table.addCell(Cells.createContent(driver.event))
            table.addCell(Cells.createContent(LongitudinalVariantInterpretation.interpret(driver as GeneAlteration)))
            table.addCell(Cells.createContent(driver.driverLikelihood.toString()))
            for (test in sortedAndFilteredTests) {
                if (testsByDriverEvent[driver.event]?.contains(test) == true) {
                    table.addCell(Cells.createContent("Detected"))
                } else {
                    table.addCell(Cells.createContent("Not detected"))
                }
            }
        }
        characteristicRow(table, sortedAndFilteredTests, "TMB") { it.characteristics.tumorMutationalBurden?.toString() ?: "" }
        characteristicRow(
            table,
            sortedAndFilteredTests,
            "MSI"
        ) { if (it.characteristics.isMicrosatelliteUnstable == false) "Stable" else "Unstable" }
        return makeWrapping(table)
    }

    private fun characteristicRow(
        table: Table,
        sortedAndFilteredTests: List<MolecularTest>,
        name: String,
        contentProvider: (MolecularTest) -> String
    ) {
        table.addCell(Cells.createContent(name))
        table.addCell(Cells.createContent(""))
        table.addCell(Cells.createContent(""))
        for (test in sortedAndFilteredTests) {
            val text = contentProvider.invoke(test)
            table.addCell(Cells.createContent(text))
        }
    }

    private fun testDisplay(test: MolecularTest): String {
        return "${test.date}\n${test.testTypeDisplay ?: test.experimentType.display()}"
    }
}