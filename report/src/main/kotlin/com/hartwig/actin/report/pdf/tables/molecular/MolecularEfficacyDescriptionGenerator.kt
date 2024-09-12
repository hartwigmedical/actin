package com.hartwig.actin.report.pdf.tables.molecular

import com.hartwig.actin.datamodel.molecular.MolecularHistory
import com.hartwig.actin.report.interpretation.ClinicalDetailsFactory
import com.hartwig.actin.report.interpretation.TreatmentEvidenceFunctions
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

        val allDrivers = DriverTableFunctions.allDrivers(molecularHistory)
            .flatMap { it.second }
            .toSortedSet(Comparator.comparing { it.event })
        val allDriverDetails = allDrivers.flatMap { ClinicalDetailsFactory(null).create(it.evidence) }
        val treatmentEvidence = allDriverDetails.map { it.treatmentEvidence }

        TreatmentEvidenceFunctions.groupByTreatment(treatmentEvidence).forEach { (treatment, evidences) ->
            val treatmentHeader = Paragraph(treatment).setBold().setFontSize(8f)
            table.addCell(Cells.createContent(treatmentHeader))
            val eventDescriptionSubTable = Table(3).setWidth(width)

            evidences.forEach { evidence ->
                val sourceEventCell = Paragraph("${evidence.sourceEvent}:").setItalic().setBold().setFontSize(7f)
                val cancerTypeCell = Paragraph(evidence.applicableCancerType.cancerType).setBold().setFontSize(6f)
                val descriptionCell = Paragraph(evidence.description).setFontSize(6.5f)

                if (evidence.direction.isResistant) {
                    descriptionCell.setFontColor(PALETTE_RED)
                }

                eventDescriptionSubTable.addCell(Cells.createContentNoBorder(sourceEventCell))
                eventDescriptionSubTable.addCell(Cells.createContentNoBorder(cancerTypeCell))
                eventDescriptionSubTable.addCell(Cells.createContentNoBorder(descriptionCell))
            }
            table.addCell(Cells.createContentNoBorder(eventDescriptionSubTable))
        }
        return table
    }
}