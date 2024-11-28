package com.hartwig.actin.report.pdf.tables.clinical

import com.hartwig.actin.datamodel.clinical.TumorDetails
import com.hartwig.actin.datamodel.molecular.MolecularRecord
import com.hartwig.actin.datamodel.molecular.orange.pharmaco.PharmacoEntry
import com.hartwig.actin.datamodel.molecular.orange.pharmaco.PharmacoGene
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

    override fun contents(): Table {
        val record = report.patientRecord
        val pharmaco = report.patientRecord.molecularHistory.latestOrangeMolecularRecord()?.pharmaco
        val mainTable = Tables.createSingleColWithWidth(700f)

        val clinicalSummaryTable = createFixedWidthCols(keyWidth / 2, valueWidth / 2, keyWidth / 2, valueWidth)
        listOf(
            "Gender" to record.patient.gender.display(),
            "Birth year" to record.patient.birthYear.toString(),
            "WHO" to whoStatus(record.clinicalStatus.who),
            "Tumor" to tumor(record.tumor),
            "Lesions" to TumorDetailsInterpreter.lesions(record.tumor),
            "Stage" to stage(record.tumor),
            "Measurable disease (RECIST)" to measurableDisease(record.tumor),
            "DPYD" to createPeachSummaryForGene(pharmaco, PharmacoGene.DPYD),
            "UGT1A1" to createPeachSummaryForGene(pharmaco, PharmacoGene.UGT1A1),
            "\n" to "\n"
        ).forEach { (key, value) ->
            clinicalSummaryTable.addCell(createKey(key))
            clinicalSummaryTable.addCell(createValue(value))
        }

        val clinicalHistoryTable = createFixedWidthCols(keyWidth, valueWidth)
        PatientClinicalHistoryGenerator(report, true, keyWidth, valueWidth).contentsAsList().forEach(clinicalHistoryTable::addCell)
        clinicalHistoryTable.addCell(createKey("Recent molecular results"))
        val molecularRecord = record.molecularHistory.latestOrangeMolecularRecord()
        clinicalHistoryTable.addCell(createValue(molecularRecord?.let(::molecularResults) ?: Formats.VALUE_NOT_AVAILABLE))

        mainTable.addCell(create(clinicalSummaryTable))
        mainTable.addCell(create(clinicalHistoryTable))
        return mainTable
    }

    private fun whoStatus(who: Int?): String {
        return who?.toString() ?: Formats.VALUE_UNKNOWN
    }

    private fun tumor(tumor: TumorDetails): String {
        val location = tumorLocation(tumor)
        val type = tumorType(tumor)
        return if (location == null || type == null) {
            Formats.VALUE_UNKNOWN
        } else {
            location + if (type.isNotEmpty()) " - $type" else ""
        }
    }

    private fun tumorLocation(tumor: TumorDetails): String? {
        return tumor.primaryTumorLocation?.let { tumorLocation ->
            val tumorSubLocation = tumor.primaryTumorSubLocation
            return if (!tumorSubLocation.isNullOrEmpty()) "$tumorLocation ($tumorSubLocation)" else tumorLocation
        }
    }

    private fun tumorType(tumor: TumorDetails): String? {
        return tumor.primaryTumorType?.let { tumorType ->
            val tumorSubType = tumor.primaryTumorSubType
            if (!tumorSubType.isNullOrEmpty()) tumorSubType else tumorType
        }
    }

    private fun stage(tumor: TumorDetails): String {
        return tumor.stage?.display() ?: Formats.VALUE_UNKNOWN
    }

    private fun geneToDrivers(drivers: List<MolecularDriverEntry>, geneToFind: String): String {
        val events = drivers.filter { it.event.contains(geneToFind) }.joinToString { it.description }
        return events.ifEmpty { "$geneToFind: No reportable events" }
    }

    private fun msStatus(molecular: MolecularRecord): String {
        return if (molecular.characteristics.isMicrosatelliteUnstable == true) "MSI" else "MSS"
    }

    private fun measurableDisease(tumor: TumorDetails): String {
        return when (tumor.hasMeasurableDisease) {
            true -> "Yes"
            false -> "No"
            else -> "NA"
        }
    }

    private fun createPeachSummaryForGene(pharmaco: Set<PharmacoEntry>?, gene: PharmacoGene): String {
        val pharmacoEntry = findPharmacoEntry(pharmaco, gene) ?: return Formats.VALUE_UNKNOWN
        return pharmacoEntry.haplotypes.joinToString(", ") { "${it.toHaplotypeString()} (${it.function.display()})" }
    }

    private fun findPharmacoEntry(pharmaco: Set<PharmacoEntry>?, geneToFind: PharmacoGene): PharmacoEntry? {
        return pharmaco?.find { it.gene == geneToFind }
    }

    private fun molecularResults(molecular: MolecularRecord): String {
        val molecularDriversInterpreter =
            MolecularDriversInterpreter(molecular.drivers, InterpretedCohortsSummarizer.fromCohorts(cohorts))
        val factory = MolecularDriverEntryFactory(molecularDriversInterpreter)
        val driverEntries = factory.create()
        val drivers = listOf("KRAS", "NRAS", "BRAF", "HER2").map { geneToDrivers(driverEntries, it) }
        return (drivers + msStatus(molecular)).joinToString(", ")
    }
}