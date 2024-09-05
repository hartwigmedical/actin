package com.hartwig.actin.report.pdf.tables.molecular

import com.hartwig.actin.datamodel.molecular.MolecularHistory
import com.hartwig.actin.datamodel.molecular.evidence.ClinicalEvidence
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

private val PRE_CLINICAL_APPROVAL_SET = setOf(
    "PRECLINICAL",
    "PRECLINICAL_PDX",
    "PRECLINICAL_BIOCHEMICAL",
    "PRECLINICAL_CELL_CULTURE",
    "PRECLINICAL_PDX_CELL_CULTURE",
    "PRECLINICAL_CELL_LINE_XENOGRAFT",
    "PRECLINICAL_PATIENT_CELL_CULTURE"
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

                        treatmentEvidencesByLevel.forEach { levelEvidences ->
                            val evidenceLevelTable = Table(1).setWidth(width / columnCount)

                            val treatmentGroupedEvidences = levelEvidences.groupBy { it.treatment }

                            treatmentGroupedEvidences.forEach { (treatment, evidencesForTreatment) ->
                                val cancerTypes =
                                    evidencesForTreatment.map { it.applicableCancerType.cancerType }.toSet().joinToString(", ")

                                val evidenceSubTable = Table(1).setWidth(width / columnCount)
                                val cancerTypeContent = Paragraph(cancerTypes).setFirstLineIndent(10f).setItalic().setFontSize(6.5f)
                                val treatmentContent = Paragraph(treatment)

                                if (evidencesForTreatment.any { it.direction.isResistant }) {
                                    treatmentContent.setFontColor(PALETTE_RED)
                                    cancerTypeContent.setFontColor(PALETTE_RED)
                                }

                                evidenceSubTable.addCell(Cells.createContentNoBorder(treatmentContent))
                                evidenceSubTable.startNewRow()
                                evidenceSubTable.addCell(Cells.createContentNoBorder(cancerTypeContent))

                                val evidenceCell = Cells.createContentNoBorder(evidenceSubTable)
                                evidenceLevelTable.addCell(evidenceCell)
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
        val treatmentEvidenceSet = evidence.treatmentEvidence
            .filter { it.onLabel == onLabel }
            .filter { it.evidenceLevel != EvidenceLevel.D || !isPreclinical(it) }.toSet()
        val (levelA, levelB, levelC, levelD) = listOf(EvidenceLevel.A, EvidenceLevel.B, EvidenceLevel.C, EvidenceLevel.D)
            .map { treatmentsForEvidenceLevelAndLabel(treatmentEvidenceSet, it) }
        return treatmentEvidenceSet.map {
            ClinicalDetails(
                it,
                levelA.contains(it.treatment),
                levelB.contains(it.treatment),
                levelC.contains(it.treatment),
                levelD.contains(it.treatment)
            )
        }.toSet()
    }

    private fun treatmentsForEvidenceLevelAndLabel(evidence: Set<TreatmentEvidence>, evidenceLevel: EvidenceLevel): Set<String> {
        return evidence
            .filter { it.evidenceLevel == evidenceLevel }
            .map { it.treatment }
            .toSet()
    }

    private fun isPreclinical(evidence: TreatmentEvidence): Boolean {
        return evidence.approvalStatus in PRE_CLINICAL_APPROVAL_SET || evidence.approvalStatus.contains("PRECLINICAL", ignoreCase = true)
    }
}