package com.hartwig.actin.report.pdf.tables.molecular

import com.hartwig.actin.molecular.datamodel.MolecularHistory
import com.hartwig.actin.molecular.datamodel.evidence.ActinEvidenceCategory
import com.hartwig.actin.molecular.datamodel.evidence.ClinicalEvidence
import com.hartwig.actin.molecular.datamodel.evidence.TreatmentEvidence
import com.hartwig.actin.report.pdf.tables.TableGenerator
import com.hartwig.actin.report.pdf.util.Cells
import com.hartwig.actin.report.pdf.util.Styles.PALETTE_YES_OR_NO_NO
import com.hartwig.serve.datamodel.EvidenceLevel
import com.itextpdf.layout.element.Table

data class ClinicalDetails(
    val treatmentEvidence: TreatmentEvidence,
    val levelA: Boolean,
    val levelB: Boolean,
    val levelC: Boolean,
    val levelD: Boolean
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
        val table = Table(columnCount).setWidth(width)
        listOf("Driver", "CKB Event", "Level A", "Level B", "Level C", "Level D")
            .map(Cells::createHeader)
            .forEach(table::addHeaderCell)

        for (driver in allDrivers) {
            val clinicalDetails = extractClinicalDetails(driver.evidence)
            if (clinicalDetails.isNotEmpty()) {
                val groupedBySourceEvent = clinicalDetails.groupBy { it.treatmentEvidence.sourceEvent }

                for ((sourceEvent, details) in groupedBySourceEvent) {
                    val treatmentEvidenceByLevel = listOf(
                        details.filter { it.levelA }.map { it.treatmentEvidence },
                        details.filter { it.levelB }.map { it.treatmentEvidence },
                        details.filter { it.levelC }.map { it.treatmentEvidence },
                        details.filter { it.levelD }.map { it.treatmentEvidence }
                    )

                    if (treatmentEvidenceByLevel.any { it.isNotEmpty() }) {
                        table.addCell(Cells.createContent(driver.event))
                        table.addCell(Cells.createContent(sourceEvent))

                        treatmentEvidenceByLevel.forEach {
                            val cellContent = it.joinToString("\n") { evidence ->
                                val treatmentText = "${evidence.treatment} (${evidence.applicableCancerType.cancerType})"
                                treatmentText
                            }
                            val cell = Cells.createContent(cellContent)

                            if (it.any { evidence -> evidence.category in
                                        setOf(ActinEvidenceCategory.SUSPECT_RESISTANT, ActinEvidenceCategory.KNOWN_RESISTANT) }) {
                                cell.setFontColor(PALETTE_YES_OR_NO_NO)
                            }
                            table.addCell(cell)
                        }
                    }
                }
            }
        }
        return table
    }

    private fun extractClinicalDetails(evidence: ClinicalEvidence): Set<ClinicalDetails> {
        val treatmentEvidenceSet = evidence.treatmentEvidence
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
            .filter { (onLabel && it.category in ON_LABEL_CATEGORIES) || (!onLabel && it.category in OFF_LABEL_CATEGORIES) }
            .map { it.treatment }
            .toSet()
    }

    private val ON_LABEL_CATEGORIES = setOf(
        ActinEvidenceCategory.ON_LABEL_EXPERIMENTAL,
        ActinEvidenceCategory.APPROVED,
        ActinEvidenceCategory.PRE_CLINICAL,
        ActinEvidenceCategory.KNOWN_RESISTANT,
        ActinEvidenceCategory.SUSPECT_RESISTANT
    )
    private val OFF_LABEL_CATEGORIES = setOf(
        ActinEvidenceCategory.APPROVED,
        ActinEvidenceCategory.OFF_LABEL_EXPERIMENTAL,
        ActinEvidenceCategory.PRE_CLINICAL,
        ActinEvidenceCategory.KNOWN_RESISTANT,
        ActinEvidenceCategory.SUSPECT_RESISTANT
    )
}