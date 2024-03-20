package com.hartwig.actin.report.pdf.tables.trial

import com.hartwig.actin.molecular.datamodel.evidence.ExternalTrial
import com.hartwig.actin.report.pdf.tables.TableGenerator
import com.hartwig.actin.report.pdf.util.Cells
import com.hartwig.actin.report.pdf.util.Styles
import com.hartwig.actin.report.pdf.util.Tables
import com.hartwig.actin.report.pdf.util.Tables.makeWrapping
import com.itextpdf.kernel.pdf.action.PdfAction
import com.itextpdf.layout.element.Table

class EligibleOtherCountriesExternalTrialsGenerator(
    private val source: String, private val externalTrialsPerEvent: Map<String, Iterable<ExternalTrial>>, private val width: Float
) : TableGenerator {
    override fun title(): String {
        return String.format(
            "%s trials potentially eligible based on molecular results which are potentially recruiting outside the Netherlands (%d)",
            source,
            externalTrialsPerEvent.values.flatten().size
        )
    }

    override fun contents(): Table {
        val eventWidth = (0.9 * width / 5).toFloat()
        val titleWidth = (2.6 * width / 5).toFloat()
        val nctWidth = (0.7 * width / 5).toFloat()
        val countriesWidth = (0.8 * width / 5).toFloat()

        val table = Tables.createFixedWidthCols(eventWidth, titleWidth + nctWidth + countriesWidth)
        table.addHeaderCell(Cells.createContentNoBorder(Cells.createHeader("Event")))
        val headerSubTable = Tables.createFixedWidthCols(titleWidth, nctWidth, countriesWidth)
        listOf("Trial title", "NCT number", "Country").forEach { headerSubTable.addHeaderCell(Cells.createHeader(it)) }
        table.addHeaderCell(Cells.createContentNoBorder(headerSubTable))

        externalTrialsPerEvent.forEach { (event, externalTrials) ->
            val subTable = Tables.createFixedWidthCols(titleWidth, nctWidth, countriesWidth)
            externalTrials.forEach { externalTrial ->
                subTable.addCell(Cells.createContentNoBorder(EligibleExternalTrialGeneratorFunctions.shortenTitle(externalTrial.title)))
                subTable.addCell(
                    Cells.createContentNoBorder(externalTrial.nctId)
                        .setAction(PdfAction.createURI(externalTrial.url))
                        .addStyle(Styles.urlStyle())
                )
                subTable.addCell(Cells.createContentNoBorder(externalTrial.countries.joinToString { it.display() }))
            }
            table.addCell(Cells.createContent(event))
            EligibleExternalTrialGeneratorFunctions.insertRow(table, subTable)
        }

        table.addCell(Cells.createSpanningSubNote(String.format("Currently only Belgian and German trials are supported"), table))

        return makeWrapping(table)
    }

}