package com.hartwig.actin.report.pdf.tables.molecular

import com.hartwig.actin.datamodel.molecular.MolecularHistory
import com.hartwig.actin.report.interpretation.TreatmentEvidenceFunctions
import com.hartwig.actin.report.interpretation.TreatmentEvidenceFunctions.filterTreatmentEvidence
import com.hartwig.actin.report.pdf.tables.TableGenerator
import com.hartwig.actin.report.pdf.util.Cells
import com.hartwig.actin.report.pdf.util.Styles
import com.hartwig.actin.report.pdf.util.Styles.PALETTE_RED
import com.hartwig.actin.report.pdf.util.Tables
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Table

class MolecularClinicalEvidenceGenerator(val molecularHistory: MolecularHistory, private val isOnLabel: Boolean) : TableGenerator {

    override fun title(): String {
        val titleEnd = "label clinical evidence"
        return if (isOnLabel) "On $titleEnd" else "Off $titleEnd"
    }

    override fun forceKeepTogether(): Boolean {
        return false
    }

    override fun contents(): Table {
        val eventWidth = (0.7 / 6).toFloat()
        val sourceEventWidth = (0.7 / 6).toFloat()
        val levelAWidth = (1.2 / 6).toFloat()
        val levelBWidth = (1.2 / 6).toFloat()
        val levelCWidth = (1.2 / 6).toFloat()
        val levelDWidth = (1.2 / 6).toFloat()

        val table = Tables.createFixedWidthCols(eventWidth, sourceEventWidth, levelAWidth, levelBWidth, levelCWidth, levelDWidth)

        listOf("Event", "CKB Event", "Level A", "Level B", "Level C", "Level D")
            .map(Cells::createHeader)
            .forEach(table::addHeaderCell)

        for ((event, evidence) in MolecularClinicalEvidenceFunctions.molecularEvidenceByEvent(molecularHistory)) {
            val filteredEvidence = filterTreatmentEvidence(evidence.treatmentEvidence, isOnLabel)
            if (filteredEvidence.isNotEmpty()) {
                val groupedBySourceEvent = TreatmentEvidenceFunctions.groupBySourceEvent(filteredEvidence)

                for ((sourceEvent, evidences) in groupedBySourceEvent) {
                    val treatmentEvidencesByLevel = TreatmentEvidenceFunctions.createPerLevelEvidenceList(evidences)

                    if (treatmentEvidencesByLevel.any { it.isNotEmpty() }) {
                        table.addCell(Cells.createContent(event))
                        table.addCell(Cells.createContent(sourceEvent))

                        treatmentEvidencesByLevel.forEach { perLevelEvidences ->
                            val evidenceLevelTable = Tables.createSingleCol()

                            val evidenceCellContents = TreatmentEvidenceFunctions.generateEvidenceCellContents(perLevelEvidences)

                            evidenceCellContents.forEach { (treatment, cancerTypes, resistance) ->
                                val cancerTypeContent = Paragraph(cancerTypes).setFirstLineIndent(5f).setFont(Styles.fontItalic()).setFontSize(5.5f)
                                val treatmentContent = Paragraph(treatment)

                                if (resistance) {
                                    treatmentContent.setFontColor(PALETTE_RED)
                                    cancerTypeContent.setFontColor(PALETTE_RED)
                                }

                                val evidenceSubTable = Tables.createSingleCol()
                                evidenceSubTable.addCell(Cells.createContentNoBorder(treatmentContent))
                                evidenceSubTable.addCell(Cells.createContentNoBorder(cancerTypeContent))

                                evidenceLevelTable.addCell(Cells.createContentNoBorder(evidenceSubTable))
                            }

                            if (evidenceLevelTable.numberOfRows == 0) {
                                evidenceLevelTable.addCell(Cells.createEmpty())
                            }

                            table.addCell(Cells.createContent(evidenceLevelTable))
                        }
                    }
                }
            }
        }
        return table
    }
}