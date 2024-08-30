package com.hartwig.actin.report.pdf.tables.molecular

import com.hartwig.actin.datamodel.molecular.MolecularHistory
import com.hartwig.actin.datamodel.molecular.evidence.ApplicableCancerType
import com.hartwig.actin.datamodel.molecular.evidence.ClinicalEvidence
import com.hartwig.actin.datamodel.molecular.evidence.EvidenceDirection
import com.hartwig.actin.datamodel.molecular.evidence.EvidenceLevel
import com.hartwig.actin.datamodel.molecular.evidence.TreatmentEvidence
import com.hartwig.actin.report.pdf.tables.TableGenerator
import com.hartwig.actin.report.pdf.util.Cells
import com.hartwig.actin.report.pdf.util.Styles.PALETTE_RED
import com.hartwig.actin.report.pdf.util.Tables
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Table

data class ClinicalDetails(
    val treatmentEvidence: TreatmentEvidence,
    val levelA: Boolean,
    val levelB: Boolean,
    val levelC: Boolean,
    val levelD: Boolean
)

private data class TreatmentEvidenceKey(
    val treatment: String,
    val onLabel: Boolean,
    val direction: EvidenceDirection,
    val isCategoryVariant: Boolean?,
    val sourceEvent: String,
    val applicableCancerType: ApplicableCancerType
)

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
            val clinicalDetails = extractClinicalDetails(driver.evidence)
            if (clinicalDetails.isNotEmpty()) {
                val groupedBySourceEvent = clinicalDetails.groupBy { it.treatmentEvidence.sourceEvent }

                for ((sourceEvent, details) in groupedBySourceEvent) {

                    val treatmentEvidencesByLevel = listOf(
                        details.filter { it.levelA }.map { it.treatmentEvidence },
                        details.filter { it.levelB }.map { it.treatmentEvidence },
                        details.filter { it.levelC }.map { it.treatmentEvidence },
                        details.filter { it.levelD }.map { it.treatmentEvidence }
                    )

                    if (treatmentEvidencesByLevel.any { it.isNotEmpty() }) {
                        table.addCell(Cells.createContent(driver.event))
                        table.addCell(Cells.createContent(sourceEvent))

                        treatmentEvidencesByLevel.forEach { perLevelEvidences ->
                            val evidenceLevelTable = Table(1).setWidth(width / columnCount)

                            perLevelEvidences.forEach { evidence ->
                                val cancerTypes = evidence.applicableCancerType.cancerType
                                val cancerTypeContent = Paragraph(cancerTypes).setFirstLineIndent(10f).setItalic().setFontSize(6.5f)
                                val treatmentContent = Paragraph(evidence.treatment)

                                if (evidence.direction.isResistant) {
                                    treatmentContent.setFontColor(PALETTE_RED)
                                    cancerTypeContent.setFontColor(PALETTE_RED)
                                }

                                val evidenceSubTable = Table(1).setWidth(width / columnCount)
                                evidenceSubTable.addCell(Cells.createContentNoBorder(treatmentContent))
                                evidenceSubTable.startNewRow()
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

    private fun extractClinicalDetails(evidence: ClinicalEvidence): Set<ClinicalDetails> {
        val treatmentEvidenceSet = evidence.treatmentEvidence.filter { it.onLabel == onLabel }.toSet()

        val groupedTreatments =
            treatmentEvidenceSet.groupBy {
                TreatmentEvidenceKey(it.treatment, it.onLabel, it.direction, it.isCategoryVariant, it.sourceEvent, it.applicableCancerType)
            }

        return groupedTreatments.flatMap { (_, treatmentEvidenceList) ->
            val (categoryVariants, nonCategoryVariants) = treatmentEvidenceList.partition { it.isCategoryVariant == true }

            val highestEvidenceLevelNonCategoryVariants = nonCategoryVariants.minOfOrNull { it.evidenceLevel }

            val highestEvidenceLevelCategoryVariants = categoryVariants.minOfOrNull { it.evidenceLevel }

            val nonCategoryVariantDetails = highestEvidenceLevelNonCategoryVariants
                ?.let { createClinicalDetails(nonCategoryVariants, it) } ?: emptyList()

            val categoryDetails = highestEvidenceLevelCategoryVariants
                ?.takeIf { level -> highestEvidenceLevelNonCategoryVariants == null || level < highestEvidenceLevelNonCategoryVariants }
                ?.let { createClinicalDetails(categoryVariants, it) }
                ?: emptyList()

            nonCategoryVariantDetails + categoryDetails
        }.toSet()
    }

    private fun createClinicalDetails(treatments: List<TreatmentEvidence>, level: EvidenceLevel): List<ClinicalDetails> =
        treatments
            .filter { it.evidenceLevel == level }
            .map { evidence ->
                ClinicalDetails(
                    evidence,
                    evidence.evidenceLevel == EvidenceLevel.A,
                    evidence.evidenceLevel == EvidenceLevel.B,
                    evidence.evidenceLevel == EvidenceLevel.C,
                    evidence.evidenceLevel == EvidenceLevel.D
                )
            }
}