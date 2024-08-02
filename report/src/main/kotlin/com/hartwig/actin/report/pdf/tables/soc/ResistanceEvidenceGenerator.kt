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
        return "Tested"
    }

    override fun contents(): Table {
        return if (treatments.isEmpty()) {
            Tables.createSingleColWithWidth(width)
                .addCell(Cells.createContentNoBorder("There are no standard of care treatment options for this patient"))
        } else {
            val table = Tables.createFixedWidthCols(120f, width - 250f).setWidth(width)
            table.addHeaderCell(Cells.createHeader("Treatment"))
            table.addHeaderCell(Cells.createHeader("Known resistance evidence"))
            treatments.sortedByDescending { it.resistanceEvidence.count() }.forEach { treatment: AnnotatedTreatmentMatch ->
                table.addCell(Cells.createContentBold(treatment.treatmentCandidate.treatment.name))
                if (treatment.resistanceEvidence.isNotEmpty()) {
                    val subtable = Tables.createFixedWidthCols(6f, 1f, 1f, 1f, 1f).setWidth(width / 4)
                    for (resistanceEvidence in treatment.resistanceEvidence) {
                        subtable.addCell(
                            Cells.createContentNoBorder(resistanceEvidence.event)
                        )

                        val iterator = resistanceEvidence.evidenceUrls.iterator()
                        var int = 1
                        while (int < 5) {
                            if (iterator.hasNext())
                                subtable.addCell(
                                    Cells.createContentNoBorder("[$int]")
                                        .setAction(PdfAction.createURI(iterator.next()))
                                        .addStyle(Styles.urlStyle())
                                )
                            else {
                                subtable.addCell(Cells.createEmpty())
                            }
                            int += 1
                        }
                    }
                    table.addCell(Cells.createContent(subtable))
                } else table.addCell(Cells.createContent("No resistance evidence"))
            }
            table
        }
    }

}