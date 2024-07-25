package com.hartwig.actin.report.pdf.tables.soc

import com.hartwig.actin.algo.datamodel.AnnotatedTreatmentMatch
import com.hartwig.actin.report.pdf.tables.TableGenerator
import com.hartwig.actin.report.pdf.util.Cells
import com.hartwig.actin.report.pdf.util.Tables
import com.itextpdf.layout.element.Table

class ResistanceEvidenceGenerator(
    private val treatments: Set<AnnotatedTreatmentMatch>,
    private val width: Float
) : TableGenerator {
    override fun title(): String {
        return "Resistance evidence"
    }

    override fun contents(): Table {
        if (treatments.isEmpty()) {
            return Tables.createSingleColWithWidth(width)
                .addCell(Cells.createContentNoBorder("There are no standard of care treatment options for this patient"))
        } else {
            val table = Tables.createFixedWidthCols(120f, width - 250f).setWidth(width)
            table.addHeaderCell(Cells.createHeader("Treatment"))
            table.addHeaderCell(Cells.createHeader("Known resistance evidence"))
            treatments.forEach { treatment: AnnotatedTreatmentMatch ->
                table.addCell(Cells.createContentBold(treatment.treatmentCandidate.treatment.name))
                if (treatment.resistanceEvidence.isNotEmpty()) {
                    val subtable = Tables.createSingleColWithWidth(width / 2)
                    for (resistanceEvidence in treatment.resistanceEvidence) {
                        subtable.addCell(Cells.create()))
                    }
                    table.addCell(Cells.createContent(subtable))
                } else table.addCell(Cells.createContent("No resistance evidence"))
            }
            return table
        }
    }

}