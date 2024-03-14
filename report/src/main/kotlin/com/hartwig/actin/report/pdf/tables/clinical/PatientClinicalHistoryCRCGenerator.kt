package com.hartwig.actin.report.pdf.tables.clinical

import com.hartwig.actin.clinical.datamodel.ClinicalRecord
import com.hartwig.actin.clinical.datamodel.TumorDetails
import com.hartwig.actin.molecular.datamodel.MolecularRecord
import com.hartwig.actin.molecular.datamodel.driver.Variant
import com.hartwig.actin.report.pdf.tables.TableGenerator
import com.hartwig.actin.report.pdf.util.Cells.create
import com.hartwig.actin.report.pdf.util.Cells.createKey
import com.hartwig.actin.report.pdf.util.Cells.createValue
import com.hartwig.actin.report.pdf.util.Formats
import com.hartwig.actin.report.pdf.util.Tables
import com.hartwig.actin.report.pdf.util.Tables.createFixedWidthCols
import com.itextpdf.layout.element.Table

class PatientClinicalHistoryCRCGenerator(private val record: ClinicalRecord, private val molecular: MolecularRecord, private val keyWidth: Float, override val valueWidth: Float) :
    TableGenerator, PatientClinicalHistoryGenerator {
    override fun title(): String {
        return "Clinical summary"
    }

    override fun contents(): Table {
        val supertable = Tables.createSingleColWithWidth(700f)

        val table = createFixedWidthCols(keyWidth/2, valueWidth/2, keyWidth/2, valueWidth)
        table.addCell(createKey("Gender"))
        table.addCell(createValue(record.patient.gender.display()))
        table.addCell(createKey("Birth year"))
        table.addCell(createValue(record.patient.birthYear.toString()))
        table.addCell(createKey("WHO"))
        table.addCell(createValue(whoStatus(record.clinicalStatus.who)))
        table.addCell(createKey("Tumor"))
        table.addCell(createValue(tumor(record.tumor)))
        table.addCell(createKey("Lesions"))
        table.addCell(createValue(lesions(record.tumor)))
        table.addCell(createKey("Stage"))
        table.addCell(createValue(stage(record.tumor)))

        val table2 = createFixedWidthCols(keyWidth, valueWidth)
        table2.addCell(createKey("\n"))
        table2.addCell(createValue("\n"))
        table2.addCell(createKey("Relevant systemic treatment history"))
        table2.addCell(create(tableOrNone(relevantSystemicPreTreatmentHistoryTable(record))))
        table2.addCell(createKey("Relevant other oncological history"))
        table2.addCell(create(tableOrNone(relevantNonSystemicPreTreatmentHistoryTable(record))))
        table2.addCell(createKey("Previous primary tumor"))
        table2.addCell(create(tableOrNone(secondPrimaryHistoryTable(record))))
        table2.addCell(createKey("Relevant non-oncological history"))
        table2.addCell(create(tableOrNone(relevantNonOncologicalHistoryTable(record))))
        table2.addCell(createKey("Recent molecular results"))
        table2.addCell(createValue(molecularResults(molecular)))

        supertable.addCell(create(table))
        supertable.addCell(create(table2))
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
            return if (variants.none { it.gene == geneToFind }) {
                "Wild-type"
            } else {
                variants.filter { it.gene == geneToFind }.joinToString { it.canonicalImpact.hgvsProteinImpact }
            }
        }

        private fun msStatus(molecular: MolecularRecord): String {
            return if (molecular.characteristics.isMicrosatelliteUnstable == true) {
                "MSI"
            } else {
                "MSS"
            }
        }

        private fun molecularResults(molecular: MolecularRecord): String {
            return "KRAS: ${geneToDrivers(molecular.drivers.variants, "KRAS")}, NRAS: ${geneToDrivers(molecular.drivers.variants, "NRAS")}, BRAF: ${geneToDrivers(molecular.drivers.variants, "BRAF")}, HER2: ${geneToDrivers(molecular.drivers.variants, "HER2")}, ${msStatus(molecular)} "
        }
    }
}