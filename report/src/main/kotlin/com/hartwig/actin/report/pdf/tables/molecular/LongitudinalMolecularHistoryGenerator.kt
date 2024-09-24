package com.hartwig.actin.report.pdf.tables.molecular

import com.hartwig.actin.datamodel.molecular.Fusion
import com.hartwig.actin.datamodel.molecular.GeneAlteration
import com.hartwig.actin.datamodel.molecular.MolecularHistory
import com.hartwig.actin.datamodel.molecular.MolecularTest
import com.hartwig.actin.datamodel.molecular.Variant
import com.hartwig.actin.report.pdf.tables.TableGenerator
import com.hartwig.actin.report.pdf.util.Cells
import com.hartwig.actin.report.pdf.util.Formats.VALUE_NOT_AVAILABLE
import com.hartwig.actin.report.pdf.util.Tables.makeWrapping
import com.itextpdf.layout.element.Table

class LongitudinalMolecularHistoryGenerator(private val molecularHistory: MolecularHistory, private val width: Float) : TableGenerator {
    override fun title(): String {
        return "Molecular history"
    }

    override fun contents(): Table {
        val sortedAndFilteredTests = molecularHistory.molecularTests.sortedBy { it.date }
        val testsWithDrivers = DriverTableFunctions.allDrivers(molecularHistory)

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
            table.addCell(Cells.createContent("${driver.event}\n(Tier ${driver.evidenceTier()})"))
            when (driver) {
                is GeneAlteration -> table.addCell(Cells.createContent(LongitudinalVariantInterpretation.interpret(driver)))
                is Fusion -> table.addCell(Cells.createContent(LongitudinalVariantInterpretation.interpret(driver)))
                else -> throw IllegalArgumentException("Unexpected driver type: ${driver::class.simpleName}")
            }
            table.addCell(Cells.createContent(driver.driverLikelihood?.toString() ?: VALUE_NOT_AVAILABLE))
            for (test in sortedAndFilteredTests) {
                if (testsByDriverEvent[driver.event]?.contains(test) == true) {
                    table.addCell(Cells.createContent("Detected${(driver as? Variant)?.let { it.variantAlleleFrequency?.let { v -> " (VAF ${v}%)" } ?: "" } ?: ""}"))
                } else {
                    table.addCell(Cells.createContent("Not detected"))
                }
            }
        }
        characteristicRow(table, sortedAndFilteredTests, "TMB") {
            it.characteristics.tumorMutationalBurden?.toString() ?: ""
        }
        characteristicRow(
            table, sortedAndFilteredTests, "MSI"
        ) {
            msiText(it)
        }
        return makeWrapping(table)
    }

    private fun msiText(it: MolecularTest) = when (it.characteristics.isMicrosatelliteUnstable) {
        false -> "Stable"
        true -> "Unstable"
        null -> ""
    }

    private fun characteristicRow(
        table: Table, sortedAndFilteredTests: List<MolecularTest>, name: String, contentProvider: (MolecularTest) -> String
    ) {
        table.addCell(Cells.createContent(name))
        table.addCell(Cells.createContent(""))
        table.addCell(Cells.createContent(""))
        for (test in sortedAndFilteredTests) {
            table.addCell(Cells.createContent(contentProvider.invoke(test)))
        }
    }

    private fun testDisplay(test: MolecularTest): String {
        return "${test.date}\n${test.testTypeDisplay ?: test.experimentType.display()}"
    }
}