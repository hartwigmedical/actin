package com.hartwig.actin.report.pdf.tables.molecular

import com.hartwig.actin.datamodel.molecular.Drivers
import com.hartwig.actin.datamodel.molecular.MolecularHistory
import com.hartwig.actin.datamodel.molecular.MolecularTest
import com.hartwig.actin.datamodel.molecular.Variant
import com.hartwig.actin.report.interpretation.InterpretedCohortsSummarizer
import com.hartwig.actin.report.interpretation.MolecularDriverEntry
import com.hartwig.actin.report.interpretation.MolecularDriverEntryFactory
import com.hartwig.actin.report.interpretation.MolecularDriversInterpreter
import com.hartwig.actin.report.pdf.tables.TableGenerator
import com.hartwig.actin.report.pdf.util.Cells
import com.hartwig.actin.report.pdf.util.Formats.VALUE_NOT_AVAILABLE
import com.hartwig.actin.report.pdf.util.Tables.makeWrapping
import com.itextpdf.layout.element.Table

class LongitudinalMolecularHistoryGenerator(private val molecularHistory: MolecularHistory, private val width: Float) : TableGenerator {

    private val driverSortOrder: Comparator<MolecularDriverEntry> = compareBy(
        MolecularDriverEntry::evidenceTier,
        MolecularDriverEntry::driverLikelihood,
        MolecularDriverEntry::gene,
        MolecularDriverEntry::eventName
    )

    override fun title(): String {
        return "Molecular history"
    }

    override fun contents(): Table {
        val eventVAFMapByTest = molecularHistory.molecularTests.sortedBy { it.date }
            .associateWith { test ->
                DriverTableFunctions.allDrivers(test).associate { it.event to (it as? Variant)?.variantAlleleFrequency }
            }

        val columnCount = 3 + eventVAFMapByTest.size
        val table = Table(columnCount).setWidth(width)

        val headers = listOf("Event", "Description", "Driver likelihood") + eventVAFMapByTest.keys.map(::testDisplay)
        headers.forEach { table.addHeaderCell(Cells.createHeader(it)) }

        val allDrivers = molecularHistory.molecularTests.map(MolecularTest::drivers).reduce(Drivers::combine)
        val molecularDriversInterpreter = MolecularDriversInterpreter(allDrivers, InterpretedCohortsSummarizer(emptyMap(), emptySet()))

        MolecularDriverEntryFactory(molecularDriversInterpreter).create()
            .sortedWith(driverSortOrder)
            .distinct()
            .flatMap { entry ->
                val driverTextFields = listOf(
                    "${entry.eventName}\n(Tier ${entry.evidenceTier})",
                    listOfNotNull(entry.driverType, entry.proteinEffect?.display()).joinToString("\n"),
                    entry.driverLikelihood?.toString() ?: VALUE_NOT_AVAILABLE
                )
                val testTextFields = eventVAFMapByTest.values.map { eventVAFMap ->
                    if (entry.eventName in eventVAFMap) {
                        eventVAFMap[entry.eventName]?.let { "VAF ${it}%" } ?: "Detected"
                    } else ""
                }
                driverTextFields + testTextFields
            }
            .forEach { table.addCell(Cells.createContent(it)) }

        characteristicRow(table, eventVAFMapByTest.keys, "TMB") {
            it.characteristics.tumorMutationalBurden?.toString() ?: ""
        }
        characteristicRow(table, eventVAFMapByTest.keys, "MSI", ::msiText)
        return makeWrapping(table)
    }

    private fun msiText(it: MolecularTest) = when (it.characteristics.isMicrosatelliteUnstable) {
        false -> "Stable"
        true -> "Unstable"
        null -> ""
    }

    private fun characteristicRow(
        table: Table, sortedAndFilteredTests: Set<MolecularTest>, name: String, contentProvider: (MolecularTest) -> String
    ) {
        (listOf(name, "", "") + sortedAndFilteredTests.map(contentProvider)).forEach { table.addCell(Cells.createContent(it)) }
    }

    private fun testDisplay(test: MolecularTest): String {
        return "${test.date}\n${test.testTypeDisplay ?: test.experimentType.display()}"
    }
}