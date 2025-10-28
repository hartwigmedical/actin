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

        // Group treatments by associated events
        val groupedRankings = treatmentEvidenceRanking.ranking.groupBy { it.events }

        // Partition groups into variants and event groups
        val (variantGroups, eventGroups) = groupedRankings.entries.partition { (event, _) ->
            event != null && event.matches(Regex(".*\\b(del|amp|fusion|mut|[A-Za-z]+\\d+)\\b.*", RegexOption.IGNORE_CASE))
        }

        // Combine groups with variants first
        val orderedGroups = variantGroups + eventGroups

        // Generate table rows for each group
        val cells = orderedGroups.flatMap { (event, treatments) ->
            val eventRow = listOf(Cells.createHeader(event ?: "Unknown Event"), Cells.createContent(""), Cells.createContent(""))
            val treatmentRows = treatments.sortedByDescending { it.score }.flatMap {
                listOf(
                    Cells.createContent(""), // Empty cell under Event
                    Cells.createContent(it.treatment),
                    Cells.createContent(DecimalFormat.getIntegerInstance().format(it.score))
                )
            }
            eventRow + treatmentRows
        }

        // Add headers and cells to the table
        (header + cells).forEach(table::addCell)
        return table
    }
}