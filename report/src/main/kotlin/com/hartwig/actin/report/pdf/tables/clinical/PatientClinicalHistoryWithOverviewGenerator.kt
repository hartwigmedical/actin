package com.hartwig.actin.report.pdf.tables.clinical

import com.hartwig.actin.clinical.datamodel.TumorDetails
import com.hartwig.actin.molecular.datamodel.MolecularRecord
import com.hartwig.actin.molecular.datamodel.driver.Variant
import com.hartwig.actin.molecular.datamodel.pharmaco.PharmacoEntry
import com.hartwig.actin.report.datamodel.Report
import com.hartwig.actin.report.pdf.tables.TableGenerator
import com.hartwig.actin.report.pdf.util.Cells.create
import com.hartwig.actin.report.pdf.util.Cells.createKey
import com.hartwig.actin.report.pdf.util.Cells.createValue
import com.hartwig.actin.report.pdf.util.Formats
import com.hartwig.actin.report.pdf.util.Tables
import com.hartwig.actin.report.pdf.util.Tables.createFixedWidthCols
import com.itextpdf.layout.element.Table

class PatientClinicalHistoryWithOverviewGenerator(
    private val report: Report, private val keyWidth: Float, val valueWidth: Float
) : TableGenerator {
    
    override fun title(): String {
        return "Clinical summary"
    }

    override fun contents(): Table {
        val record = report.patientRecord
        val pharmaco = report.patientRecord.molecularHistory.latestMolecularRecord()?.pharmaco
        val supertable = Tables.createSingleColWithWidth(700f)

        val clinicalSummaryTable = createFixedWidthCols(keyWidth / 2, valueWidth / 2, keyWidth / 2, valueWidth)
        clinicalSummaryTable.addCell(createKey("Gender"))
        clinicalSummaryTable.addCell(createValue(record.patient.gender.display()))
        clinicalSummaryTable.addCell(createKey("Birth year"))
        clinicalSummaryTable.addCell(createValue(record.patient.birthYear.toString()))
        clinicalSummaryTable.addCell(createKey("WHO"))
        clinicalSummaryTable.addCell(createValue(whoStatus(record.clinicalStatus.who)))
        clinicalSummaryTable.addCell(createKey("Tumor"))
        clinicalSummaryTable.addCell(createValue(tumor(record.tumor)))
        clinicalSummaryTable.addCell(createKey("Lesions"))
        clinicalSummaryTable.addCell(createValue(lesions(record.tumor)))
        clinicalSummaryTable.addCell(createKey("Stage"))
        clinicalSummaryTable.addCell(createValue(stage(record.tumor)))
        clinicalSummaryTable.addCell(createKey("Measurable disease (RECIST)"))
        clinicalSummaryTable.addCell(createValue(measurableDisease(record.tumor)))
        clinicalSummaryTable.addCell(createKey("DPYD"))
        clinicalSummaryTable.addCell(createValue(createPeachSummaryForGene(pharmaco, "DPYD")))
        clinicalSummaryTable.addCell(createKey("UGT1A1"))
        clinicalSummaryTable.addCell(createValue(createPeachSummaryForGene(pharmaco, "UGT1A1")))

        val clinicalHistoryTable = createFixedWidthCols(keyWidth, valueWidth)
        clinicalHistoryTable.addCell(createKey("\n"))
        clinicalHistoryTable.addCell(createValue("\n"))
        PatientClinicalHistoryGenerator(report, true, keyWidth, valueWidth).contentsAsList().forEach(clinicalHistoryTable::addCell)
        clinicalHistoryTable.addCell(createKey("Recent molecular results"))
        val molecularRecord = record.molecularHistory.latestMolecularRecord()
        clinicalHistoryTable.addCell(createValue(molecularRecord?.let(::molecularResults) ?: Formats.VALUE_NOT_AVAILABLE))

        supertable.addCell(create(clinicalSummaryTable))
        supertable.addCell(create(clinicalHistoryTable))
        return supertable
    }

    companion object {
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

        fun lesions(tumor: TumorDetails): String {
            val categorizedLesions = listOf(
                "CNS" to tumor.hasCnsLesions,
                "Brain" to tumor.hasBrainLesions,
                "Liver" to tumor.hasLiverLesions,
                "Bone" to tumor.hasBoneLesions,
                "Lung" to tumor.hasLungLesions
            ).filter { it.second == true }.map { it.first }

            val lesions =
                listOfNotNull(categorizedLesions, tumor.otherLesions, listOfNotNull(tumor.biopsyLocation)).flatten()
                    .sorted().distinctBy { it.uppercase() }

            val (lymphNodeLesions, otherLesions) = lesions.partition { it.lowercase().startsWith("lymph node") }

            val filteredLymphNodeLesions = lymphNodeLesions.map { lesion ->
                lesion.split(" ").filterNot { it.lowercase() in setOf("lymph", "node", "nodes", "") }.joinToString(" ")
            }.filterNot(String::isEmpty).distinctBy(String::lowercase)

            val lymphNodeLesionsString = if (filteredLymphNodeLesions.isNotEmpty()) {
                listOf("Lymph nodes (${filteredLymphNodeLesions.joinToString(", ")})")
            } else if (lymphNodeLesions.isNotEmpty()) {
                listOf("Lymph nodes")
            } else emptyList()

            return if (lesions.isEmpty()) {
                Formats.VALUE_UNKNOWN
            } else {
                (otherLesions + lymphNodeLesionsString).joinToString(", ")
            }
        }

        private fun geneToDrivers(variants: Set<Variant>, geneToFind: String): String {
            val drivers = if (variants.none { it.gene == geneToFind }) {
                "Wild-type"
            } else {
                variants.filter { it.gene == geneToFind }.joinToString { it.canonicalImpact.hgvsProteinImpact }
            }
            return "$geneToFind: $drivers"
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

        private fun createPeachSummaryForGene(pharmaco: Set<PharmacoEntry>?, gene: String): String {
            val pharmacoEntry = findPharmacoEntry(pharmaco, gene) ?: return Formats.VALUE_UNKNOWN
            return pharmacoEntry.haplotypes.joinToString(", ") { "${it.name} (${it.function})" }
        }

        private fun findPharmacoEntry(pharmaco: Set<PharmacoEntry>?, geneToFind: String): PharmacoEntry? {
            return pharmaco?.find { it.gene == geneToFind }
        }

        private fun molecularResults(molecular: MolecularRecord): String {
            val drivers = listOf("KRAS", "NRAS", "BRAF", "HER2").map { geneToDrivers(molecular.drivers.variants, it) }
            return (drivers + msStatus(molecular)).joinToString(", ")
        }
    }
}