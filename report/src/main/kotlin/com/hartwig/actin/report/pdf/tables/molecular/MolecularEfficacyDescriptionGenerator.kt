package com.hartwig.actin.report.pdf.tables.molecular

import com.hartwig.actin.datamodel.molecular.MolecularHistory
import com.hartwig.actin.report.interpretation.TreatmentEvidenceFunctions
import com.hartwig.actin.report.interpretation.TreatmentEvidenceFunctions.filterTreatmentEvidence
import com.hartwig.actin.report.interpretation.TreatmentEvidenceFunctions.sortTreatmentEvidence
import com.hartwig.actin.report.pdf.tables.TableGenerator
import com.hartwig.actin.report.pdf.util.Cells
import com.hartwig.actin.report.pdf.util.Styles.PALETTE_RED
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Table

class MolecularEfficacyDescriptionGenerator(val molecularHistory: MolecularHistory, private val width: Float) : TableGenerator {

    override fun title(): String {
        return "Efficacy evidence description"
    }

    override fun contents(): Table {
        val table = Table(1).setWidth(width)

        val allDrivers = DriverTableFunctions.allDrivers(molecularHistory).flatMap { it.second }
        val filteredEvidence = allDrivers
            .flatMap { filterTreatmentEvidence(it.evidence.treatmentEvidence, null) }
        val sortedEvidence = sortTreatmentEvidence(filteredEvidence)

        TreatmentEvidenceFunctions.groupBySourceEvent(sortedEvidence).forEach { (event, evidences) ->
            val eventHeader = Paragraph(event).setBold().setFontSize(8f)
            table.addCell(Cells.createContent(eventHeader))
            val eventDescriptionSubTable = Table(4).setWidth(width)

            evidences.forEach { evidence ->
                val treatmentCell = Paragraph("${evidence.treatment}:").setItalic().setBold().setFontSize(7f)
                val evidenceLevelAndDateCell = Paragraph("Level ${evidence.evidenceLevel.name} (${evidence.date.year})").setFontSize(6f)
                val cancerTypeCell = Paragraph(evidence.applicableCancerType.cancerType).setBold().setFontSize(6f)
                val descriptionCell = Paragraph(evidence.description).setFontSize(6.5f)

                if (evidence.direction.isResistant) {
                    descriptionCell.setFontColor(PALETTE_RED)
                }

                eventDescriptionSubTable.addCell(Cells.createContentNoBorder(treatmentCell))
                eventDescriptionSubTable.addCell(Cells.createContentNoBorder(evidenceLevelAndDateCell))
                eventDescriptionSubTable.addCell(Cells.createContentNoBorder(cancerTypeCell))
                eventDescriptionSubTable.addCell(Cells.createContentNoBorder(descriptionCell))
            }
            table.addCell(Cells.createContentNoBorder(eventDescriptionSubTable))
        }
        return table
    }
}