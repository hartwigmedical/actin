package com.hartwig.actin.report.pdf.tables.treatment

import com.google.common.collect.Multimap
import com.hartwig.actin.molecular.datamodel.evidence.ExternalTrial
import com.hartwig.actin.report.pdf.tables.TableGenerator
import com.hartwig.actin.report.pdf.util.Cells
import com.hartwig.actin.report.pdf.util.Formats.COMMA_SEPARATOR
import com.hartwig.actin.report.pdf.util.Styles
import com.hartwig.actin.report.pdf.util.Tables
import com.hartwig.actin.report.pdf.util.Tables.makeWrapping
import com.itextpdf.kernel.pdf.action.PdfAction
import com.itextpdf.layout.element.Table

class EligibleOtherCountriesExternalTrialsGenerator(
    private val source: String, private val externalTrialsPerEvent: Multimap<String, ExternalTrial>, private val width: Float
) : TableGenerator {
    override fun title(): String {
        return String.format(
            "%s trials potentially eligible based on molecular results which are potentially recruiting outside the Netherlands (%d)",
            source,
            externalTrialsPerEvent.size()
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

        for (event in externalTrialsPerEvent.keySet()) {
            val subTable = Tables.createFixedWidthCols(titleWidth, nctWidth, countriesWidth)
            externalTrialsPerEvent[event].forEach {
                subTable.addCell(Cells.createContentNoBorder(EligibleExternalTrialGeneratorFunctions.shortenTitle(it.title())))
                subTable.addCell(
                    Cells.createContentNoBorder(it.nctId()).setAction(PdfAction.createURI(it.url()))
                        .addStyle(
                            Styles.urlStyle()
                        )
                )
                subTable.addCell(Cells.createContentNoBorder(it.countries().joinToString(COMMA_SEPARATOR)))

            }
            table.addCell(
                Cells.createContent(event)
            )
            EligibleExternalTrialGeneratorFunctions.insertRow(table, subTable)
        }

        return makeWrapping(table)
    }

}