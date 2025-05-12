package com.hartwig.actin.report.pdf.tables.molecular

import com.hartwig.actin.report.pdf.tables.TableGenerator
import com.hartwig.actin.report.pdf.util.Cells
import com.hartwig.actin.report.pdf.util.Tables
import com.hartwig.actin.treatment.TreatmentEvidenceRanking
import com.itextpdf.layout.element.Table
import java.text.DecimalFormat

class TreatmentRankingGenerator(private val treatmentEvidenceRanking: TreatmentEvidenceRanking) : TableGenerator {

    override fun title() = "Treatment ranking"

    override fun forceKeepTogether() = false

    override fun contents(): Table {
        val table = Tables.createRelativeWidthCols(1f, 1f, 1f)
        val header = listOf("Treatment", "Events", "Score").map(Cells::createHeader)
        val cells = treatmentEvidenceRanking.ranking.sortedBy { it.score }.reversed().flatMap {
            listOf(it.treatment, it.events.joinToString("\n"), DecimalFormat.getIntegerInstance().format(it.score))
        }.map(Cells::createContent)
        (header + cells).forEach(table::addCell)
        return table
    }
}