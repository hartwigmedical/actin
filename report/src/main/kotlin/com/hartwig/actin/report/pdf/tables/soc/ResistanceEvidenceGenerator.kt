package com.hartwig.actin.report.pdf.tables.soc

import com.hartwig.actin.algo.datamodel.AnnotatedTreatmentMatch
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
        return "Resistance evidence"
    }

    override fun contents(): Table {
        return if (treatments.isEmpty()) {
            Tables.createSingleColWithWidth(width)
                .addCell(Cells.createContentNoBorder("There are no standard of care treatment options for this patient"))
        } else {
            val table = Tables.createFixedWidthCols(1f, 3f, 1f).setWidth(width)
            table.addHeaderCell(Cells.createHeader("Treatment"))
            table.addHeaderCell(Cells.createHeader("Resistance evidence"))
            table.addHeaderCell(Cells.createHeader("Found in molecular analysis"))
            val treatmentToEvidence = treatments.flatMap { it.resistanceEvidence }.groupBy({ it.treatmentName }, { it })

            treatmentToEvidence.forEach { entry ->
                table.addCell(Cells.createContentBold(entry.key))
                val subtable = Tables.createFixedWidthCols(3f, 1f, 1f, 1f, 75f, 1f).setWidth(width / 2)
                for (resistanceEvidence in entry.value.distinct().sortedBy { it.event }) {
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

                    subtable.addCell(Cells.createContentNoBorder(booleanToString(resistanceEvidence.isFound)))
                }
                table.addCell(Cells.createContent(subtable))
                table.addCell(Cells.createContent(""))
            }
            table
        }
    }

    companion object {
        private fun booleanToString(isFound: Boolean?): String {
            return when (isFound) {
                true -> "Yes"
                false -> "No"
                null -> ""
            }
        }
    }

}