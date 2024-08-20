package com.hartwig.actin.report.pdf.tables.molecular

import com.hartwig.actin.molecular.datamodel.Driver
import com.hartwig.actin.molecular.datamodel.MolecularHistory
import com.hartwig.actin.molecular.datamodel.evidence.ActionableEvidence
import com.hartwig.actin.report.pdf.tables.TableGenerator
import com.hartwig.actin.report.pdf.util.Cells
import com.hartwig.serve.datamodel.EvidenceLevel
import com.itextpdf.layout.element.Table

data class ClinicalDetails(
    val treatment: String,
    val evidenceLevelA: Boolean,
    val evidenceLevelB: Boolean,
    val evidenceLevelC: Boolean,
    val evidenceLevelD: Boolean,
)

const val CHECKED = "X"
const val UNCHECKED = ""

class MolecularClinicalEvidenceGenerator(val molecularHistory: MolecularHistory, private val width: Float) : TableGenerator {

    override fun title(): String {
        return "Clinical evidence"
    }

    override fun contents(): Table {
        val allDrivers =
            DriverTableFunctions.allDrivers(molecularHistory).flatMap { it.second }.toSortedSet(Comparator.comparing { it.event })
        val columnCount = 6
        val table = Table(columnCount).setWidth(width)
        listOf(
            "Variant", "Treatment", "Level A", "Level B", "Level C", "Level D"
        )
            .map(Cells::createHeader)
            .forEach(table::addHeaderCell)

        val driverWithClinicalDetails = allDrivers.map { it to extractClinicalDetails(it.evidence) }.sortedWith(comparator())
        for ((driver, clinicalDetails) in driverWithClinicalDetails) {
            if (clinicalDetails.isNotEmpty()) {
                table.addCell(Cells.createContent("${driver.event} - Tier ${driver.evidence.evidenceTier()}"))
                for ((rowCount, clinicalDetail) in clinicalDetails.withIndex()) {
                    val rowContent = with(clinicalDetail) {
                        listOf(treatment) + listOf(
                            evidenceLevelA,
                            evidenceLevelB,
                            evidenceLevelC,
                            evidenceLevelD
                        ).map(::formatBoolean)
                    }
                    val cells = if (rowCount > 0) {
                        listOf(Cells.createEmpty()) + rowContent.map(Cells::createContentLightBorder)
                    } else {
                        rowContent.map(Cells::createContent)
                    }
                    cells.forEach(table::addCell)
                }
            }
        }
        return table
    }

    private fun comparator() =
        Comparator.comparing<Pair<Driver, Set<ClinicalDetails>>?, Boolean?> { it.second.any { c -> c.evidenceLevelA } }
            .thenComparing(Comparator.comparing { it.second.any { c -> c.evidenceLevelB } })
            .thenComparing(Comparator.comparing { it.second.any { c -> c.evidenceLevelC } })
            .thenComparing(Comparator.comparing { it.second.any { c -> c.evidenceLevelD } }).reversed()

    private fun formatBoolean(boolean: Boolean) = if (boolean) CHECKED else UNCHECKED

    private fun extractClinicalDetails(evidence: ActionableEvidence): Set<ClinicalDetails> {
        val evidenceLevelA = truncatedTreatments(treatmentsForEvidenceLevel(evidence, EvidenceLevel.A))
        val evidenceLevelB = truncatedTreatments(treatmentsForEvidenceLevel(evidence, EvidenceLevel.B))
        val evidenceLevelC = truncatedTreatments(treatmentsForEvidenceLevel(evidence, EvidenceLevel.C))
        val evidenceLevelD = truncatedTreatments(treatmentsForEvidenceLevel(evidence, EvidenceLevel.D))
        val allTreatments =
            evidenceLevelA + evidenceLevelB + evidenceLevelC + evidenceLevelD
        return allTreatments.map {
            ClinicalDetails(
                it,
                evidenceLevelA.contains(it),
                evidenceLevelB.contains(it),
                evidenceLevelC.contains(it),
                evidenceLevelD.contains(it),
            )
        }.toSet()
    }

    private fun treatmentsForEvidenceLevel(evidence: ActionableEvidence, evidenceLevel: EvidenceLevel) =
        evidence.actionableTreatments.filter { it.evidenceLevel == evidenceLevel }.map { it.name }.toSet()

    private fun truncatedTreatments(treatments: Set<String>) = if (treatments.size > 4) listOf("<many>") else treatments.toList()
}