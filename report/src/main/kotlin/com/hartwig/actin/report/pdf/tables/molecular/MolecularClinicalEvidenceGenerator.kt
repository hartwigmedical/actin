package com.hartwig.actin.report.pdf.tables.molecular

import com.hartwig.actin.molecular.datamodel.MolecularHistory
import com.hartwig.actin.molecular.datamodel.evidence.ActionableEvidence
import com.hartwig.actin.report.pdf.tables.TableGenerator
import com.hartwig.actin.report.pdf.util.Cells
import com.hartwig.actin.report.pdf.util.Tables
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
        val allDrivers = DriverTableFunctions.allDrivers(molecularHistory).flatMap { it.second }.toSet()
        val columnCount = 7
        val columnWidth = width / columnCount
        val table =
            Tables.createFixedWidthCols(columnWidth, columnWidth * 2, columnWidth, columnWidth, columnWidth, columnWidth, columnWidth)
        table.addHeaderCell(Cells.createHeader("Variant"))
        table.addHeaderCell(Cells.createHeader("Treatment"))
        table.addHeaderCell(Cells.createHeader("Approved"))
        table.addHeaderCell(Cells.createHeader("On-label Experimental"))
        table.addHeaderCell(Cells.createHeader("Off-label Experimental"))
        table.addHeaderCell(Cells.createHeader("Pre-clinical"))
        table.addHeaderCell(Cells.createHeader("Resistant"))
        for (driver in allDrivers) {
            val clinicalDetails = extractClinicalDetails(driver.evidence)
            if (clinicalDetails.isNotEmpty()) {
                table.addCell(Cells.createContent(driver.event))
                for ((rowCount, clinicalDetail) in clinicalDetails.withIndex()) {
                    if (rowCount > 0) {
                        table.addCell(Cells.createEmpty())
                        table.addCell(Cells.createContentLightBorder(clinicalDetail.treatment))
                        table.addCell(Cells.createContentLightBorder(formatBoolean(clinicalDetail.approved)))
                        table.addCell(Cells.createContentLightBorder(formatBoolean(clinicalDetail.onLabel)))
                        table.addCell(Cells.createContentLightBorder(formatBoolean(clinicalDetail.offLabel)))
                        table.addCell(Cells.createContentLightBorder(formatBoolean(clinicalDetail.preClinical)))
                        table.addCell(Cells.createContentLightBorder(formatBoolean(clinicalDetail.resistant)))
                    } else {
                        table.addCell(Cells.createContent(clinicalDetail.treatment))
                        table.addCell(Cells.createContent(formatBoolean(clinicalDetail.approved)))
                        table.addCell(Cells.createContent(formatBoolean(clinicalDetail.onLabel)))
                        table.addCell(Cells.createContent(formatBoolean(clinicalDetail.offLabel)))
                        table.addCell(Cells.createContent(formatBoolean(clinicalDetail.preClinical)))
                        table.addCell(Cells.createContent(formatBoolean(clinicalDetail.resistant)))
                    }
                }
            }
        }
        return table
    }

    private fun formatBoolean(boolean: Boolean) = if (boolean) CHECKED else UNCHECKED

    private fun extractClinicalDetails(evidence: ActionableEvidence): Set<ClinicalDetails> {
        val truncatedApproved = truncatedTreatments(evidence.approvedTreatments)
        val truncatedOnLabel = truncatedTreatments(evidence.onLabelExperimentalTreatments)
        val truncatedOffLabel = truncatedTreatments(evidence.offLabelExperimentalTreatments)
        val truncatedPreClinical = truncatedTreatments(evidence.preClinicalTreatments)
        val truncatedResistant = truncatedTreatments(evidence.knownResistantTreatments)
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