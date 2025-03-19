package com.hartwig.actin.report.pdf.tables.molecular

import com.hartwig.actin.datamodel.algo.TreatmentEvidenceRanking
import com.hartwig.actin.report.pdf.tables.TableGenerator
import com.hartwig.actin.report.pdf.util.Cells
import com.itextpdf.layout.element.Table

class TreatmentRankingGenerator(private val treatmentEvidenceRanking: TreatmentEvidenceRanking) : TableGenerator {
    override fun title() = "Treatment ranking"

    override fun contents(): Table {
        val table = Table(3)
        val header = listOf("Event", "Treatment", "Score").map(Cells::createHeader)
        val cells = treatmentEvidenceRanking.ranking.flatMap {
            listOf(it.treatment, it.event, it.score.toString())
        }.map(Cells::createContent)
        (header + cells).forEach(table::addCell)
        return table
    }
}