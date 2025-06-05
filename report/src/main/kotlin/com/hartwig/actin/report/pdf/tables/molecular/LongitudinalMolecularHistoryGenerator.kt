package com.hartwig.actin.report.pdf.tables.molecular

import com.hartwig.actin.datamodel.molecular.MolecularHistory
import com.hartwig.actin.datamodel.molecular.MolecularTest
import com.hartwig.actin.datamodel.molecular.driver.Drivers
import com.hartwig.actin.datamodel.molecular.driver.Variant
import com.hartwig.actin.report.interpretation.InterpretedCohort
import com.hartwig.actin.report.interpretation.InterpretedCohortsSummarizer
import com.hartwig.actin.report.interpretation.MolecularDriverEntry
import com.hartwig.actin.report.interpretation.MolecularDriverEntryFactory
import com.hartwig.actin.report.interpretation.MolecularDriversInterpreter
import com.hartwig.actin.report.pdf.tables.TableGenerator
import com.hartwig.actin.report.pdf.util.Cells
import com.hartwig.actin.report.pdf.util.Formats.VALUE_NOT_AVAILABLE
import com.hartwig.actin.report.pdf.util.Tables
import com.itextpdf.layout.element.Table

class LongitudinalMolecularHistoryGenerator(private val molecularHistory: MolecularHistory, private val cohorts: List<InterpretedCohort>) :
    TableGenerator {

    private val driverSortOrder: Comparator<MolecularDriverEntry> = compareBy(
        MolecularDriverEntry::evidenceTier,
        MolecularDriverEntry::driverLikelihoodDisplay,
        MolecularDriverEntry::event
    )

    override fun title(): String {
        return "Molecular history"
    }

    override fun forceKeepTogether(): Boolean {
        return false
    }

    override fun contents(): Table {
        val eventVAFMapByTest = molecularHistory.molecularTests.sortedBy { it.date }
            .associateWith { test ->
                DriverTableFunctions.allDrivers(test).associate { it.event to (it as? Variant)?.variantAlleleFrequency }
            }

        val finalColumnWidths = FloatArray(eventVAFMapByTest.size) { 0.8f }
        val table = Tables.createRelativeWidthCols(1.3f, 1.3f, 0.8f, *finalColumnWidths)

        val headers = listOf("Event", "Description", "Driver likelihood") + eventVAFMapByTest.keys.map(::testDisplay)
        headers.forEach { table.addHeaderCell(Cells.createHeader(it)) }

        val allDrivers = molecularHistory.molecularTests.map(MolecularTest::drivers).reduce(Drivers::combine)
        val molecularDriversInterpreter = MolecularDriversInterpreter(allDrivers, InterpretedCohortsSummarizer.fromCohorts(cohorts))

        MolecularDriverEntryFactory(molecularDriversInterpreter).create()
            .sortedWith(driverSortOrder)
            .distinct()
            .flatMap { entry ->
                val driverTextFields = listOf(
                    "${entry.event}\n(Tier ${entry.evidenceTier})",
                    listOfNotNull(
                        entry.driverType,
                        if (entry.driverType.contains(entry.proteinEffect?.display() ?: "", true)) null else entry.proteinEffect?.display()
                    ).joinToString("\n"),
                    entry.driverLikelihood?.toString() ?: VALUE_NOT_AVAILABLE
                )
                val testTextFields = eventVAFMapByTest.values.map { eventVAFMap ->
                    if (entry.event in eventVAFMap) {
                        eventVAFMap[entry.event]?.let { "VAF ${it}%" } ?: "Detected"
                    } else ""
                }
                driverTextFields + testTextFields
            }
            .forEach { table.addCell(Cells.createContent(it)) }

        characteristicRow(table, eventVAFMapByTest.keys, "TMB") {
            it.characteristics.tumorMutationalBurden?.score?.toString() ?: ""
        }
        characteristicRow(table, eventVAFMapByTest.keys, "MSI", ::msiText)
        return table
    }

    private fun msiText(it: MolecularTest) = when (it.characteristics.microsatelliteStability?.isUnstable) {
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