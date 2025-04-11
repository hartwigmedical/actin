package com.hartwig.actin.report.pdf.tables.soc

import com.hartwig.actin.report.datamodel.Report
import com.hartwig.actin.report.pdf.tables.TableGenerator
import com.hartwig.actin.report.pdf.tables.soc.SOCGeneratorFunctions.approvedTreatmentCells
import com.hartwig.actin.report.pdf.util.Cells
import com.hartwig.actin.report.pdf.util.Tables
import com.itextpdf.layout.element.Table

class SOCEligibleApprovedTreatmentGenerator(private val report: Report) : TableGenerator {

    override fun title(): String {
        return "Standard of care options considered potentially eligible"
    }

    override fun forceKeepTogether(): Boolean {
        return false
    }

    override fun contents(): Table {
        val treatments = report.treatmentMatch.standardOfCareMatches?.filter { it.eligible() }
        if (treatments.isNullOrEmpty()) {
            return Tables.createSingleCol()
                .addCell(Cells.createContentNoBorder("There are no standard of care treatment options for this patient"))
        }
        val table = Tables.createFixedWidthCols(0.18f, 0.30f, 0.25f, 0.27f)
        sequenceOf("Treatment", "Literature efficacy evidence", "Real-world efficacy evidence", "Warnings")
            .map(Cells::createHeader)
            .forEach(table::addHeaderCell)

        approvedTreatmentCells(treatments).forEach(table::addCell)
        return table
    }
}