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

    /*
    override fun contents(): Table {
        val table = Tables.createRelativeWidthCols(1f, 1f, 1f)
        val header = listOf("Treatment", "Events", "Score").map(Cells::createHeader)
        val cells = treatmentEvidenceRanking.ranking.sortedBy { it.score }.reversed().flatMap {
            listOf(it.treatment, it.events.toString(), DecimalFormat.getIntegerInstance().format(it.score))
        }.map(Cells::createContent)
        (header + cells).forEach(table::addCell)
        return table
    }
    */

    override fun contents(): Table {
        val table = Tables.createRelativeWidthCols(1f, 1f, 1f)
        val header = listOf("Event / Variant", "Treatment", "Score").map(Cells::createHeader)

        // Group treatments by their associated events
        val groupedRankings = treatmentEvidenceRanking.ranking.groupBy { it.events }

        // For each event group, sort treatments by score and prepare table cells
        val cells = groupedRankings.flatMap { (event, treatments) ->
            // Add a row for the event (spanning columns if necessary)
            val eventRow = listOf(Cells.createHeader(event ?: "Unknown Event"), Cells.createContent(""), Cells.createContent(""))

            // Process and rank treatments within this event group
            val treatmentRows = treatments.sortedByDescending { it.score }.flatMap {
                listOf(
                    Cells.createContent(""), // Empty cell under Event
                    Cells.createContent(it.treatment),
                    Cells.createContent(DecimalFormat.getIntegerInstance().format(it.score))
                )
            }

            eventRow + treatmentRows
        }

        // Add header and cells to the table
        (header + cells).forEach(table::addCell)
        return table
    }
}