package com.hartwig.actin.report.pdf.tables.molecular

import com.hartwig.actin.report.pdf.tables.TableGenerator
import com.hartwig.actin.report.pdf.util.Cells
import com.hartwig.actin.report.pdf.util.Formats
import com.hartwig.actin.report.pdf.util.Styles.PALETTE_MID_GREY
import com.hartwig.actin.report.pdf.util.Tables
import com.hartwig.actin.treatment.TreatmentEvidenceRanking
import com.itextpdf.layout.borders.SolidBorder
import com.itextpdf.layout.element.Table

class TreatmentRankingGenerator(private val treatmentEvidenceRanking: TreatmentEvidenceRanking) : TableGenerator {

    override fun title() = "Treatment ranking"

    override fun forceKeepTogether() = false

    override fun contents(): Table {
        val table = Tables.createRelativeWidthCols(1f, 1f, 1f)
        val header = listOf("Event", "Treatment", "Score").map(Cells::createHeader)
        
        val groupedRankings = treatmentEvidenceRanking.ranking.groupBy { it.events }
        
        val cells = groupedRankings.flatMap { (events, treatments) ->
            
            val eventRow = listOf(
                Cells.createContent(events.joinToString("\n")),
                Cells.createEmpty().apply { setBorderTop(SolidBorder(PALETTE_MID_GREY, 0.25f)) },
                Cells.createEmpty().apply { setBorderTop(SolidBorder(PALETTE_MID_GREY, 0.25f)) }
            )
            val treatmentRows = treatments.sortedByDescending { it.score }.flatMap { treatment ->
                listOf(
                    Cells.createEmpty(),
                    Cells.createContentNoBorder(treatment.treatment), 
                    Cells.createContentNoBorder(Formats.noDigitNumber(treatment.score)) 
                )
            }
            eventRow + treatmentRows
        }
        
        (header + cells).forEach(table::addCell)
        return table
    }
}