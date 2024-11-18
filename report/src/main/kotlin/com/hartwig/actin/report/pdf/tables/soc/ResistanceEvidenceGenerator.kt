package com.hartwig.actin.report.pdf.tables.soc

import com.hartwig.actin.datamodel.algo.AnnotatedTreatmentMatch
import com.hartwig.actin.report.pdf.tables.TableGenerator
import com.hartwig.actin.report.pdf.util.Cells
import com.hartwig.actin.report.pdf.util.Styles
import com.hartwig.actin.report.pdf.util.Tables
import com.itextpdf.kernel.pdf.action.PdfAction
import com.itextpdf.layout.element.Table

class ResistanceEvidenceGenerator(
    private val treatments: Set<AnnotatedTreatmentMatch>,
    private val width: Float
) : TableGenerator {

    override fun title(): String {
        return ""
    }

    override fun contents(): Table {
        return if (treatments.isEmpty()) {
            Tables.createSingleColWithWidth(width)
                .addCell(Cells.createContentNoBorder("There are no standard of care treatment options for this patient"))
        } else {
            val treatmentToEvidence = treatments.flatMap { it.resistanceEvidence }.groupBy({ it.treatmentName }, { it })
            if (treatmentToEvidence.isEmpty()) {
                Tables.createSingleColWithWidth(width)
                    .addCell(
                        Cells.createContentNoBorder(
                            "No resistance evidence found for the standard of care treatment options of this patient"
                        )
                    )
            } else {
                val table = Tables.createFixedWidthCols(3f, 2f, 3f, 2f).setWidth(width)
                table.addHeaderCell(Cells.createHeader("Treatment"))
                table.addHeaderCell(Cells.createHeader("Mutation"))
                table.addHeaderCell(Cells.createHeader("Evidence level"))
                table.addHeaderCell(Cells.createHeader("Found in molecular analysis"))
                treatmentToEvidence.forEach { entry ->
                    table.addCell(Cells.createContentBold(entry.key))
                    val subtable = Tables.createFixedWidthCols(100f, 22f, 22f, 22f, 125f, 70f, 10f).setWidth((width / 3) * 2)
                    for (resistanceEvidence in entry.value.distinct().sortedBy { it.resistanceLevel }) {
                        subtable.addCell(Cells.createContentNoBorder(resistanceEvidence.event))

                        resistanceEvidence.evidenceUrls.forEachIndexed { index, url ->
                            if (index < 4) {
                                subtable.addCell(
                                    Cells.createContentNoBorder("[${index + 1}]")
                                        .setAction(PdfAction.createURI(url))
                                        .addStyle(Styles.urlStyle())
                                )
                            }
                        }

                        repeat(4 - resistanceEvidence.evidenceUrls.size) {
                            subtable.addCell(Cells.createEmpty())
                        }

                        subtable.addCell(Cells.createContentNoBorder(resistanceEvidence.resistanceLevel))
                        subtable.addCell(Cells.createContentNoBorder(booleanToString(resistanceEvidence.isFound)))
                    }
                    table.addCell(Cells.createContent(subtable))
                    table.addCell(Cells.createContent(""))
                    table.addCell(Cells.createContent(""))
                }
                table
            }
        }
    }

    private fun booleanToString(isFound: Boolean?): String {
        return when (isFound) {
            true -> "Yes"
            false -> "No"
            null -> "NA"
        }
    }
}