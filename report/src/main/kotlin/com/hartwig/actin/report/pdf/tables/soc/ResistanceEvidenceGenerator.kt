package com.hartwig.actin.report.pdf.tables.soc

import com.hartwig.actin.datamodel.algo.AnnotatedTreatmentMatch
import com.hartwig.actin.report.pdf.ReportLabels
import com.hartwig.actin.report.pdf.tables.TableGenerator
import com.hartwig.actin.report.pdf.util.Cells
import com.hartwig.actin.report.pdf.util.Styles
import com.hartwig.actin.report.pdf.util.Tables
import com.itextpdf.kernel.pdf.action.PdfAction
import com.itextpdf.layout.element.Table

class ResistanceEvidenceGenerator(
    private val treatments: Set<AnnotatedTreatmentMatch>,
    private val width: Float,
    private val labels: ReportLabels
) : TableGenerator {

    override fun title(): String {
        return labels.socResistanceTitle()
    }

    override fun forceKeepTogether(): Boolean {
        return false
    }

    override fun contents(): Table {
        return if (treatments.isEmpty()) {
            Tables.createSingleColWithWidth(width)
                .addCell(Cells.createContentNoBorder(labels.socNoOptions()))
        } else {
            val treatmentToEvidence = treatments.flatMap { it.resistanceEvidence }.groupBy({ it.treatmentName }, { it })
            if (treatmentToEvidence.isEmpty()) {
                Tables.createSingleColWithWidth(width)
                    .addCell(
                        Cells.createContentNoBorder(labels.socNoResistance())
                    )
            } else {
                val table = Tables.createRelativeWidthCols(3f, 3f, 2f, 2f, 3f).setWidth(width)
                table.addHeaderCell(Cells.createHeader(labels.socColTreatment()))
                table.addHeaderCell(Cells.createHeader(labels.socColMutation()))
                table.addHeaderCell(Cells.createHeader(labels.socColEvidenceSource()))
                table.addHeaderCell(Cells.createHeader(labels.socColEvidenceLevel()))
                table.addHeaderCell(Cells.createHeader(labels.socColFoundInMolecular()))
                treatmentToEvidence.forEach { entry ->
                    table.addCell(Cells.createContentBold(entry.key))
                    val subTable = Tables.createRelativeWidthCols(660f, 1f, 1f, 1f, 250f, 400f, 400f).setWidth((width / 3) * 2)
                    for (resistanceEvidence in entry.value.distinct().sortedBy { it.resistanceLevel }) {
                        subTable.addCell(Cells.createContentNoBorder(resistanceEvidence.event))

                        resistanceEvidence.evidenceUrls.forEachIndexed { index, url ->
                            if (index < 4) {
                                subTable.addCell(
                                    Cells.createContentNoBorder("[${index + 1}]")
                                        .setAction(PdfAction.createURI(url))
                                        .addStyle(Styles.urlStyle())
                                )
                            }
                        }

                        repeat(4 - resistanceEvidence.evidenceUrls.size) {
                            subTable.addCell(Cells.createContentNoBorder("[0]").setFontColor(Styles.PALETTE_WHITE))
                        }

                        subTable.addCell(Cells.createContentNoBorder(resistanceEvidence.resistanceLevel))
                        subTable.addCell(Cells.createContentNoBorder(booleanToString(resistanceEvidence.isFound)))
                    }
                    table.addCell(Cells.createContent(subTable))
                    table.addCell(Cells.createContent(""))
                    table.addCell(Cells.createContent(""))
                    table.addCell(Cells.createContent(""))
                }
                table
            }
        }
    }

    private fun booleanToString(isFound: Boolean?): String {
        return when (isFound) {
            true -> labels.miscYes()
            false -> labels.miscNo()
            null -> labels.miscNotAvailable()
        }
    }
}
