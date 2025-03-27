package com.hartwig.actin.report.pdf.tables.molecular

import com.hartwig.actin.datamodel.molecular.MolecularHistory
import com.hartwig.actin.datamodel.molecular.evidence.TreatmentEvidence
import com.hartwig.actin.report.interpretation.TreatmentEvidenceFunctions
import com.hartwig.actin.report.interpretation.TreatmentEvidenceFunctions.filterTreatmentEvidence
import com.hartwig.actin.report.interpretation.TreatmentEvidenceFunctions.sortTreatmentEvidence
import com.hartwig.actin.report.pdf.tables.TableGenerator
import com.hartwig.actin.report.pdf.util.Cells
import com.hartwig.actin.report.pdf.util.Styles
import com.hartwig.actin.report.pdf.util.Styles.PALETTE_RED
import com.itextpdf.layout.element.Cell
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Table

class MolecularEfficacyDescriptionGenerator(val molecularHistory: MolecularHistory, private val width: Float) : TableGenerator {

    override fun title(): String {
        return "Efficacy evidence description"
    }

    override fun contents(): Table {
        val table = Table(1).setWidth(width)
        val sortedEvidence = DriverTableFunctions.allDrivers(molecularHistory)
            .flatMap { it.second }
            .flatMap { filterTreatmentEvidence(it.evidence.treatmentEvidence, null) }
            .let { sortTreatmentEvidence(it) }

        val cells = TreatmentEvidenceFunctions.groupBySourceEvent(sortedEvidence)
            .flatMap { (event, evidences) -> createEventCells(event, evidences) }

        cells.forEach(table::addCell)

        return table
    }

    private fun createEventCells(event: String, evidences: List<TreatmentEvidence>): List<Cell> {
        val eventHeaderCell = Cells.createContent(Paragraph(event).setFont(Styles.fontBold()).setFontSize(8f))
        val eventSubTableCell = createEventSubTable(evidences)
        return listOf(eventHeaderCell, eventSubTableCell)
    }

    private fun createEventSubTable(evidences: List<TreatmentEvidence>): Cell {
        val subTable = Table(4).setWidth(width)
        evidences.flatMap { createEvidenceCells(it) }.forEach(subTable::addCell)
        return Cells.createContentNoBorder(subTable)
    }

    private fun createEvidenceCells(evidence: TreatmentEvidence): List<Cell> {
        val treatmentCell = Cells.createContentNoBorder(Paragraph("${evidence.treatment}:").setFont(Styles.fontItalicBold()).setFontSize(7f))
        val evidenceLevelAndDateCell = Cells.createContentNoBorder(
            Paragraph("Level ${evidence.evidenceLevel.name} (${evidence.evidenceYear})").setFontSize(6f)
        )
        val cancerTypeCell =
            Cells.createContentNoBorder(Paragraph(evidence.applicableCancerType.matchedCancerType).setFont(Styles.fontBold()).setFontSize(6f))

        val descriptionCell = Paragraph(evidence.efficacyDescription).setFontSize(6.5f)

        if (evidence.evidenceDirection.isResistant) {
            descriptionCell.setFontColor(PALETTE_RED)
        }

        val descriptionContentCell = Cells.createContentNoBorder(descriptionCell)

        return listOf(treatmentCell, evidenceLevelAndDateCell, cancerTypeCell, descriptionContentCell)
    }
}