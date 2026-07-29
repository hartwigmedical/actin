package com.hartwig.actin.report.pdf.tables.soc

import com.hartwig.actin.report.datamodel.Report
import com.hartwig.actin.report.pdf.ReportLabels
import com.hartwig.actin.report.pdf.tables.TableGenerator
import com.hartwig.actin.report.pdf.util.Cells
import com.hartwig.actin.report.pdf.util.Tables
import com.itextpdf.layout.element.Table

class EligibleStandardOfCareGenerator(private val report: Report, private val labels: ReportLabels) : TableGenerator {

    override fun title(): String {
        return labels.socEligibleTitle()
    }

    override fun forceKeepTogether(): Boolean {
        return false
    }

    override fun contents(): Table {
        val treatments = report.treatmentMatch.standardOfCareMatches?.filter { it.eligible() }
        if (treatments.isNullOrEmpty()) {
            return Tables.createSingleCol()
                .addCell(Cells.createContentNoBorder(labels.socNoOptions()))
        }
        val table = Tables.createRelativeWidthCols(18f, 30f, 27f)
        sequenceOf(labels.socColTreatment(), labels.socColLiteratureEvidence(), labels.socColWarnings())
            .map(Cells::createHeader).forEach(table::addHeaderCell)

        SoCGeneratorFunctions.approvedTreatmentCells(treatments).forEach(table::addCell)
        return table
    }
}
