package com.hartwig.actin.report.pdf.tables.soc

import com.hartwig.actin.algo.datamodel.AnnotatedTreatmentMatch
import com.hartwig.actin.report.datamodel.Report
import com.hartwig.actin.report.pdf.tables.TableGenerator
import com.hartwig.actin.report.pdf.tables.soc.SOCGeneratorFunctions.approvedTreatmentCells
import com.hartwig.actin.report.pdf.util.Cells
import com.hartwig.actin.report.pdf.util.Tables
import com.itextpdf.layout.element.Table

class SOCEligibleApprovedTreatmentGenerator(
    private val report: Report,
    private val width: Float
) : TableGenerator {

    override fun title(): String {
        return "Standard of care options considered potentially eligible"
    }

    override fun contents(): Table {
        val treatments = report.treatmentMatch.standardOfCareMatches?.filter(AnnotatedTreatmentMatch::eligible)
        if (treatments.isNullOrEmpty()) {
            return Tables.createSingleColWithWidth(width)
                .addCell(Cells.createContentNoBorder("There are no standard of care treatment options for this patient"))
        }
        val table = Table(3).setWidth(width)
        table.addHeaderCell(Cells.createHeader("Treatment"))
        table.addHeaderCell(Cells.createHeader("Literature efficacy evidence"))
        table.addHeaderCell(Cells.createHeader("Warnings"))

        approvedTreatmentCells(treatments).forEach(table::addCell)
        return table
    }
}