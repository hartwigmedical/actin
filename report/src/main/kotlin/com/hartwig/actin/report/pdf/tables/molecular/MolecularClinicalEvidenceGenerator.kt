package com.hartwig.actin.report.pdf.tables.molecular

import com.hartwig.actin.molecular.datamodel.MolecularHistory
import com.hartwig.actin.molecular.datamodel.evidence.ActionableEvidence
import com.hartwig.actin.report.pdf.tables.TableGenerator
import com.hartwig.actin.report.pdf.util.Cells
import com.itextpdf.layout.element.Table

data class ClinicalDetails(
    val treatment: String,
    val approved: Boolean,
    val onLabel: Boolean,
    val offLabel: Boolean,
    val preClinical: Boolean,
    val resistant: Boolean
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
        val columnCount = 7
        val table = Table(columnCount).setWidth(width)
        listOf(
            "Variant", "Treatment", "Approved", "On-label Experimental", "Off-label Experimental", "Pre-clinical", "Resistant"
        )
            .map(Cells::createHeader)
            .forEach(table::addHeaderCell)
        for (driver in allDrivers) {
            val clinicalDetails = extractClinicalDetails(driver.evidence)
            if (clinicalDetails.isNotEmpty()) {
                table.addCell(Cells.createContent(driver.event))
                for ((rowCount, clinicalDetail) in clinicalDetails.withIndex()) {
                    val rowContent = with(clinicalDetail) {
                        listOf(treatment) + listOf(
                            approved,
                            onLabel,
                            offLabel,
                            preClinical,
                            resistant
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

    private fun formatBoolean(boolean: Boolean) = if (boolean) CHECKED else UNCHECKED

    private fun extractClinicalDetails(evidence: ActionableEvidence): Set<ClinicalDetails> {
        val truncatedApproved = truncatedTreatments(evidence.approvedTreatments())
        val truncatedOnLabel = truncatedTreatments(evidence.onLabelExperimentalTreatments())
        val truncatedOffLabel = truncatedTreatments(evidence.offLabelExperimentalTreatments())
        val truncatedPreClinical = truncatedTreatments(evidence.preClinicalTreatments())
        val truncatedResistant = truncatedTreatments(evidence.knownResistant())
        val allTreatments =
            truncatedApproved + truncatedOnLabel + truncatedOffLabel + truncatedPreClinical + truncatedResistant
        return allTreatments.map {
            ClinicalDetails(
                it,
                truncatedApproved.contains(it),
                truncatedOnLabel.contains(it),
                truncatedOffLabel.contains(it),
                truncatedPreClinical.contains(it),
                truncatedResistant.contains(it)
            )
        }.toSet()
    }

    private fun truncatedTreatments(treatments: Set<String>) = if (treatments.size > 2) listOf("<many>") else treatments.toList()
}