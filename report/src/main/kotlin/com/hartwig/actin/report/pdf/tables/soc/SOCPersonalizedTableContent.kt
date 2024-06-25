package com.hartwig.actin.personalization.similarity.report

import com.hartwig.actin.personalization.datamodel.Measurement
import com.hartwig.actin.personalization.datamodel.MeasurementType
import com.hartwig.actin.personalization.datamodel.SubPopulationAnalysis
import com.hartwig.actin.report.pdf.util.Cells
import com.hartwig.actin.report.pdf.util.Styles
import com.hartwig.actin.report.pdf.util.Styles.BORDER
import com.itextpdf.layout.element.Cell
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Table
import com.itextpdf.layout.element.Text
import java.lang.IllegalArgumentException

const val FIRST_COLUMN_FRACTIONAL_WIDTH = 0.15

data class TableElement(val boldContent: String? = null, val content: String? = null) {

    fun toCell(): Cell {
        val textElements = listOfNotNull(
            boldContent?.let { Text(it).addStyle(Styles.tableHighlightStyle()) },
            content?.let { Text(it).addStyle(Styles.tableContentStyle()) },
        )
        val paragraph = Paragraph()
        paragraph.addAll(textElements)
        return Cells.create(paragraph)
    }

    companion object {
        fun regular(content: String) = TableElement(content = content)
    }
}

data class SOCPersonalizedTableContent(val headers: List<String>, val rows: List<List<TableElement>>) {

    fun check() {
        if (rows.any { it.size != headers.size }) {
            throw IllegalArgumentException("All rows must have the same number of columns as the headers")
        }
    }

    fun render(width: Float): Table {
        val firstColumnWidth = (width * FIRST_COLUMN_FRACTIONAL_WIDTH).toFloat()
        val numPopulationColumns = headers.size - 1
        val populationColumnWidth = (width - firstColumnWidth) / numPopulationColumns
        val columnWidths = floatArrayOf(firstColumnWidth) + FloatArray(numPopulationColumns) { populationColumnWidth }
        val table = Table(columnWidths)
        headers.map { Cells.createHeader(it).setBorderBottom(BORDER) }.forEach(table::addHeaderCell)

        rows.flatMap { rowElements ->
            rowElements.map(TableElement::toCell).let { listOf(it.first().setBorderRight(BORDER)) + it.drop(1) }
        }
            .forEach(table::addCell)

        return table
    }

    companion object {
        fun fromSubPopulationAnalyses(
            subPopulationAnalyses: List<SubPopulationAnalysis>,
            eligibleTreatments: Set<String>,
            measurementType: MeasurementType,
            createTableElement: (Measurement) -> TableElement
        ): SOCPersonalizedTableContent {
            val headers = listOf("") + subPopulationAnalyses.map {
                "${it.name} (n=${it.treatmentMeasurements[measurementType]?.numPatients})"
            }
            val measurementsBySubPopulationName = subPopulationAnalyses.associate { (name, treatmentMeasurements) ->
                name to treatmentMeasurements[measurementType]!!.measurementsByTreatment
            }
            val rows = subPopulationAnalyses.first().treatments.filter { it.name.lowercase() in eligibleTreatments }
                .map { treatment ->
                    val rowValues = subPopulationAnalyses.map { subPopulation ->
                        createTableElement(measurementsBySubPopulationName[subPopulation.name]!![treatment]!!)
                    }
                    listOf(TableElement.regular(treatment.display())) + rowValues
                }
            return SOCPersonalizedTableContent(headers, rows)
        }
    }
}
