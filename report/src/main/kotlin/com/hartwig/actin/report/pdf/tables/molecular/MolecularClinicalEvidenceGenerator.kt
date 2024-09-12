package com.hartwig.actin.report.pdf.tables.molecular

import com.hartwig.actin.datamodel.molecular.MolecularHistory
import com.hartwig.actin.report.interpretation.ClinicalDetailsFactory
import com.hartwig.actin.report.interpretation.ClinicalDetailsFunctions
import com.hartwig.actin.report.interpretation.TreatmentEvidenceFunctions
import com.hartwig.actin.report.pdf.tables.TableGenerator
import com.hartwig.actin.report.pdf.util.Cells
import com.hartwig.actin.report.pdf.util.Styles.PALETTE_RED
import com.hartwig.actin.report.pdf.util.Tables
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Table

class MolecularClinicalEvidenceGenerator(
    val molecularHistory: MolecularHistory,
    private val width: Float,
    private val onLabel: Boolean = true
) : TableGenerator {

    override fun title(): String {
        val titleEnd = "label clinical evidence"
        return if (onLabel) "On $titleEnd" else "Off $titleEnd"
    }

    override fun contents(): Table {
        val allDrivers =
            DriverTableFunctions.allDrivers(molecularHistory).flatMap { it.second }.toSortedSet(Comparator.comparing { it.event })
        val columnCount = 6

        val eventWidth = (0.7 * width / 6).toFloat()
        val sourceEventWidth = (0.7 * width / 6).toFloat()
        val levelAWidth = (1.2 * width / 6).toFloat()
        val levelBWidth = (1.2 * width / 6).toFloat()
        val levelCWidth = (1.2 * width / 6).toFloat()
        val levelDWidth = (1.2 * width / 6).toFloat()

        val table = Tables.createFixedWidthCols(eventWidth, sourceEventWidth, levelAWidth, levelBWidth, levelCWidth, levelDWidth)

        listOf("Driver", "CKB Event", "Level A", "Level B", "Level C", "Level D")
            .map(Cells::createHeader)
            .forEach(table::addHeaderCell)

        for (driver in allDrivers) {
            val clinicalDetails = ClinicalDetailsFactory(onLabel).create(driver.evidence)
            if (clinicalDetails.isNotEmpty()) {
                val groupedBySourceEvent = ClinicalDetailsFunctions.groupBySourceEvent(clinicalDetails)

                for ((sourceEvent, details) in groupedBySourceEvent) {
                    val treatmentEvidencesByLevel = ClinicalDetailsFunctions.mapTreatmentEvidencesToLevel(details)

                    if (treatmentEvidencesByLevel.any { it.isNotEmpty() }) {
                        table.addCell(Cells.createContent(driver.event))
                        table.addCell(Cells.createContent(sourceEvent))

                        treatmentEvidencesByLevel.forEach { perLevelEvidences ->
                            val evidenceLevelTable = Table(1).setWidth(width / columnCount)

                            val evidenceCellContents = TreatmentEvidenceFunctions.generateEvidenceCellContents(perLevelEvidences)

                            evidenceCellContents.forEach { (treatment, cancerTypes, resistance) ->
                                val cancerTypeContent = Paragraph(cancerTypes).setFirstLineIndent(5f).setItalic().setFontSize(5.5f)
                                val treatmentContent = Paragraph(treatment)

                                if (resistance) {
                                    treatmentContent.setFontColor(PALETTE_RED)
                                    cancerTypeContent.setFontColor(PALETTE_RED)
                                }

                                val evidenceSubTable = Table(1).setWidth(width / columnCount)
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