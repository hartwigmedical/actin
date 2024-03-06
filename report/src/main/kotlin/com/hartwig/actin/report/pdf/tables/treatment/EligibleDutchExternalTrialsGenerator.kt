package com.hartwig.actin.report.pdf.tables.treatment

import com.hartwig.actin.molecular.datamodel.evidence.ExternalTrial
import com.hartwig.actin.report.pdf.tables.TableGenerator
import com.hartwig.actin.report.pdf.util.Cells
import com.hartwig.actin.report.pdf.util.Styles
import com.hartwig.actin.report.pdf.util.Tables
import com.hartwig.actin.report.pdf.util.Tables.makeWrapping
import com.itextpdf.kernel.pdf.action.PdfAction
import com.itextpdf.layout.element.Table

class EligibleDutchExternalTrialsGenerator(
    private val source: String, private val externalTrialsPerEvent: Map<String, List<ExternalTrial>>, private val width: Float
) : TableGenerator {
    override fun title(): String {
        return String.format(
            "%s trials potentially eligible based on molecular results which are potentially recruiting in The Netherlands (%d)",
            source,
            externalTrialsPerEvent.values.flatten().size
        )
    }

    override fun contents(): Table {
        val eventWidth = (0.9 * width / 5).toFloat()
        val titleWidth = (3.4 * width / 5).toFloat()
        val nctWidth = (0.7 * width / 5).toFloat()

        val table = Tables.createFixedWidthCols(eventWidth, titleWidth + nctWidth)
        table.addHeaderCell(Cells.createContentNoBorder(Cells.createHeader("Event")))
        val headerSubTable = Tables.createFixedWidthCols(titleWidth, nctWidth)
        listOf("Trial title", "NCT number").forEach { headerSubTable.addHeaderCell(Cells.createHeader(it)) }
        table.addHeaderCell(Cells.createContentNoBorder(headerSubTable))

        val trialsGroupedByTitle = externalTrialsPerEvent.flatMap { it.value }.groupBy { it.title }
        val combinedEventsPerTrial = mutableMapOf<String, MutableList<String>>()

        externalTrialsPerEvent.forEach { (event, externalTrials) ->
            val subTable = Tables.createFixedWidthCols(titleWidth, nctWidth)
            externalTrials.forEach { trial ->
                if ((trialsGroupedByTitle[trial.title]?.size ?: 0) > 1) {
                    combinedEventsPerTrial.getOrPut(trial.title) { mutableListOf() }.add(event)
                } else {
                    subTable.addCell(Cells.createContentNoBorder(trial.title))
                    subTable.addCell(
                        Cells.createContentNoBorder(trial.nctId).setAction(PdfAction.createURI(trial.url)).addStyle(Styles.urlStyle())
                    )
                }
            }
            if (subTable.numberOfRows > 0) {
                table.addCell(Cells.createContent(event))
                EligibleExternalTrialGeneratorFunctions.insertRow(table, subTable)
            }
        }

        combinedEventsPerTrial.forEach { (title, events) ->
            val combinedEvents = events.joinToString(", \n")
            val combinedEventsTitle = trialsGroupedByTitle[title]?.firstOrNull()
            val subTable = Tables.createFixedWidthCols(titleWidth, nctWidth)
            combinedEventsTitle?.let {
                subTable.addCell(Cells.createContentNoBorder(combinedEventsTitle.title))
                subTable.addCell(
                    Cells.createContentNoBorder(combinedEventsTitle.nctId).setAction(PdfAction.createURI(combinedEventsTitle.url)).addStyle(Styles.urlStyle())
                )
            }
            table.addCell(Cells.createContent(combinedEvents))
            EligibleExternalTrialGeneratorFunctions.insertRow(table, subTable)
        }

        return makeWrapping(table)
    }
}