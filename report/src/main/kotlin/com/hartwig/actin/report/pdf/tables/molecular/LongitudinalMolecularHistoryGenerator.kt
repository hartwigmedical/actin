package com.hartwig.actin.report.pdf.tables.molecular

import com.hartwig.actin.datamodel.molecular.Driver
import com.hartwig.actin.datamodel.molecular.Fusion
import com.hartwig.actin.datamodel.molecular.GeneAlteration
import com.hartwig.actin.datamodel.molecular.MolecularHistory
import com.hartwig.actin.datamodel.molecular.MolecularTest
import com.hartwig.actin.datamodel.molecular.Variant
import com.hartwig.actin.datamodel.molecular.orange.driver.CopyNumber
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
        val sortedAndFilteredTests =
            molecularHistory.molecularTests.sortedBy { it.date }
                .associateWith { DriverTableFunctions.allDrivers(it).associate { d -> d.event to (d as? Variant)?.variantAlleleFrequency } }
        val testsWithDrivers = DriverTableFunctions.allDrivers(molecularHistory)

        val allDrivers =
            testsWithDrivers.flatMap { it.second.map { d -> (d as? Variant)?.copy(variantAlleleFrequency = null) ?: d } }.toSet()
                .sortedWith(driverSortOrder())
        val columnCount = 3 + sortedAndFilteredTests.size
        val table = Table(columnCount).setWidth(width)

        table.addHeaderCell(Cells.createHeader("Event"))
        table.addHeaderCell(Cells.createHeader("Description"))
        table.addHeaderCell(Cells.createHeader("Driver likelihood"))

        for (test in sortedAndFilteredTests) {
            table.addHeaderCell(Cells.createHeader(testDisplay(test.key)))
        }

        for (driver in allDrivers) {
            table.addCell(Cells.createContent("${driver.event}\n(Tier ${driver.evidenceTier()})"))
            when (driver) {
                is Variant -> table.addCell(Cells.createContent(LongitudinalDriverInterpretation.interpret(driver)))
                is CopyNumber -> table.addCell(Cells.createContent(LongitudinalDriverInterpretation.interpret(driver)))
                is Fusion -> table.addCell(Cells.createContent(LongitudinalDriverInterpretation.interpret(driver)))
                else -> throw IllegalArgumentException("Unexpected driver type: ${driver::class.simpleName}")
            }
            table.addCell(Cells.createContent(driver.driverLikelihood?.toString() ?: VALUE_NOT_AVAILABLE))
            for (test in sortedAndFilteredTests) {
                if (test.value.containsKey(driver.event)) {
                    val vafInTest = test.value[driver.event]
                    table.addCell(Cells.createContent("Detected${vafInTest?.let { v -> " (VAF ${v}%)" } ?: ""}"))
                } else {
                    table.addCell(Cells.createContent("Not detected"))
                }
            }
        }
        characteristicRow(table, sortedAndFilteredTests.keys, "TMB") {
            it.characteristics.tumorMutationalBurden?.toString() ?: ""
        }
        characteristicRow(
            table, sortedAndFilteredTests.keys, "MSI"
        ) {
            msiText(it)
        }
        return makeWrapping(table)
    }

    private fun driverSortOrder(): Comparator<Driver> = compareBy(
        { it.evidenceTier() },
        { it.driverLikelihood },
        {
            when (it) {
                is Fusion -> it.geneStart
                is GeneAlteration -> it.gene
                else -> null
            }
        },
        { it.event }
    )

    private fun msiText(it: MolecularTest) = when (it.characteristics.isMicrosatelliteUnstable) {
        false -> "Stable"
        true -> "Unstable"
        null -> ""
    }

    private fun characteristicRow(
        table: Table, sortedAndFilteredTests: Set<MolecularTest>, name: String, contentProvider: (MolecularTest) -> String
    ) {
        table.addCell(Cells.createContent(name))
        table.addCell(Cells.createContent(""))
        table.addCell(Cells.createContent(""))
        for (test in sortedAndFilteredTests.sortedBy { it.date }) {
            table.addCell(Cells.createContent(contentProvider.invoke(test)))
        }
    }

    private fun testDisplay(test: MolecularTest): String {
        return "${test.date}\n${test.testTypeDisplay ?: test.experimentType.display()}"
    }
}