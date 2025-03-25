package com.hartwig.actin.report.pdf.tables.molecular

import com.hartwig.actin.datamodel.algo.TreatmentEvidenceRanking
import com.hartwig.actin.report.pdf.tables.TableGenerator
import com.hartwig.actin.report.pdf.util.Cells
import com.hartwig.actin.report.pdf.util.Tables
import com.itextpdf.layout.element.Table

class TreatmentRankingGenerator(private val treatmentEvidenceRanking: TreatmentEvidenceRanking, private val width: Float) : TableGenerator {
    override fun title() = "Treatment ranking"

    override fun contents(): Table {
        val columnWidth = width / 3
        val table = Tables.createFixedWidthCols(columnWidth, columnWidth, columnWidth)
        val header = listOf("Treatment", "Events", "Score").map(Cells::createHeader)
        val cells = treatmentEvidenceRanking.ranking.sortedBy { it.score }.reversed().flatMap {
            listOf(it.treatment, it.events.joinToString(), it.score.toString())
        }.map(Cells::createContentNoBorder)
        (header + cells).forEach(table::addCell)
        return table
    }
}