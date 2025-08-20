package com.hartwig.actin.report.pdf.tables.clinical

import com.hartwig.actin.datamodel.clinical.TumorDetails
import com.hartwig.actin.datamodel.molecular.panel.PanelRecord
import com.hartwig.actin.datamodel.molecular.pharmaco.PharmacoEntry
import com.hartwig.actin.datamodel.molecular.pharmaco.PharmacoGene
import com.hartwig.actin.report.datamodel.Report
import com.hartwig.actin.report.interpretation.InterpretedCohort
import com.hartwig.actin.report.interpretation.InterpretedCohortsSummarizer
import com.hartwig.actin.report.interpretation.MolecularDriverEntry
import com.hartwig.actin.report.interpretation.MolecularDriverEntryFactory
import com.hartwig.actin.report.interpretation.MolecularDriversInterpreter
import com.hartwig.actin.report.interpretation.TumorDetailsInterpreter
import com.hartwig.actin.report.pdf.tables.TableGenerator
import com.hartwig.actin.report.pdf.util.Cells.create
import com.hartwig.actin.report.pdf.util.Cells.createKey
import com.hartwig.actin.report.pdf.util.Cells.createValue
import com.hartwig.actin.report.pdf.util.Formats
import com.hartwig.actin.report.pdf.util.Tables
import com.hartwig.actin.report.pdf.util.Tables.createFixedWidthCols
import com.itextpdf.layout.element.Table

class PatientClinicalHistoryWithOverviewGenerator(
    private val report: Report, private val cohorts: List<InterpretedCohort>, private val keyWidth: Float, val valueWidth: Float
) : TableGenerator {

    override fun title(): String {
        return "Clinical summary"
    }

    override fun forceKeepTogether(): Boolean {
        return true
    }

    override fun contents(): Table {
        val record = report.patientRecord
        val pharmaco =
            report.patientRecord.molecularHistory.latestOrangeMolecularRecord()?.pharmaco //TODO: Currently no pharmaco in panel record?
        val table = Tables.createSingleColWithWidth(keyWidth + valueWidth)

        val clinicalSummaryTable = createFixedWidthCols(keyWidth / 2, valueWidth / 2, keyWidth / 2, valueWidth / 2)
        listOf(
            "Gender (birth year, WHO)" to "${record.patient.gender.display()} (${record.patient.birthYear}, WHO ${whoStatus(record.performanceStatus.latestWho)})",
            "Stage" to stage(record.tumor),
            "Tumor" to record.tumor.name,
            "DPYD" to createPeachSummaryForGene(pharmaco, PharmacoGene.DPYD),
            "Lesions" to TumorDetailsInterpreter.lesionString(record.tumor),
            "UGT1A1" to createPeachSummaryForGene(pharmaco, PharmacoGene.UGT1A1),
            "Measurable (RECIST)" to measurableDisease(record.tumor)
        ).forEach { (key, value) ->
            clinicalSummaryTable.addCell(createKey(key))
            clinicalSummaryTable.addCell(createValue(value))
        }

        val clinicalHistoryTable = createFixedWidthCols(keyWidth, valueWidth)
        PatientClinicalHistoryGenerator(report, true, keyWidth, valueWidth).contentsAsList().forEach(clinicalHistoryTable::addCell)
        val molecularRecord = report.patientRecord.molecularHistory.allPanels().firstOrNull()
        clinicalHistoryTable.addCell(createKey("Recent molecular results"))
        clinicalHistoryTable.addCell(createValue(molecularRecord?.let(::molecularResults) ?: Formats.VALUE_NOT_AVAILABLE))

        table.addCell(create(clinicalSummaryTable))
        table.addCell(create(clinicalHistoryTable))
        return table
    }

    private fun whoStatus(who: Int?): String {
        return who?.toString() ?: Formats.VALUE_UNKNOWN
    }

    private fun stage(tumor: TumorDetails): String {
        return tumor.stage?.display() ?: Formats.VALUE_UNKNOWN
    }

    private fun geneToDrivers(drivers: List<MolecularDriverEntry>, geneToFind: String): String {
        val events = drivers.filter { it.event.contains(geneToFind) }.joinToString { it.description }
        return events.ifEmpty { "$geneToFind: No reportable events" }
    }

    private fun msStatus(molecular: PanelRecord): String {
        return if (molecular.characteristics.microsatelliteStability?.isUnstable == true) "MSI" else "MSS"
    }

    private fun measurableDisease(tumor: TumorDetails): String {
        return when (tumor.hasMeasurableDisease) {
            true -> "Yes"
            false -> "No"
            else -> "NA"
        }
    }

    private fun createPeachSummaryForGene(pharmaco: Set<PharmacoEntry>?, gene: PharmacoGene): String {
        val pharmacoEntry = findPharmacoEntry(pharmaco, gene) ?: return Formats.VALUE_NOT_AVAILABLE
        return pharmacoEntry.haplotypes.joinToString(", ") { "${it.toHaplotypeString()} (${it.function.display()})" }
    }

    private fun findPharmacoEntry(pharmaco: Set<PharmacoEntry>?, geneToFind: PharmacoGene): PharmacoEntry? {
        return pharmaco?.find { it.gene == geneToFind }
    }

    private fun molecularResults(molecular: PanelRecord): String {
        val molecularDriversInterpreter =
            MolecularDriversInterpreter(molecular.drivers, InterpretedCohortsSummarizer.fromCohorts(cohorts))
        val factory = MolecularDriverEntryFactory(molecularDriversInterpreter)
        val driverEntries = factory.create()
        val drivers = listOf("KRAS", "NRAS", "BRAF", "HER2").map { geneToDrivers(driverEntries, it) }
        return (drivers + msStatus(molecular)).joinToString(", ")
    }
}