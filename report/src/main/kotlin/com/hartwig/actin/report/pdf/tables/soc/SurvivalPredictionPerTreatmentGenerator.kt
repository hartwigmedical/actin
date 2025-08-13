package com.hartwig.actin.report.pdf.tables.soc

import com.hartwig.actin.report.interpretation.soc.SurvivalPredictionEntryFactory
import com.hartwig.actin.report.pdf.tables.TableGenerator
import com.hartwig.actin.report.pdf.util.Cells
import com.hartwig.actin.report.pdf.util.Formats
import com.hartwig.actin.report.pdf.util.Tables
import com.itextpdf.layout.element.Table
import jetbrains.letsPlot.geom.geomLine
import jetbrains.letsPlot.export.ggsave
import com.itextpdf.layout.element.IElement
import com.itextpdf.svg.converter.SvgConverter
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

class SurvivalPredictionPerTreatmentGenerator(private val survivalPredictions: Map<String, List<Double>>) :
    TableGenerator {

    override fun title(): String {
        return "Survival predictions per treatment"
    }

    override fun forceKeepTogether(): Boolean {
        return true
    }

    override fun contents(): Table {
            val data = mapOf(
        "x" to listOf(0, 1, 2, 3, 4),
        "y" to listOf(0, 1, 4, 9, 16)
    )
    val plot = letsPlot(data) + geomLine {
        x = "x"
        y = "y"
    }

    // 2. Export plot to SVG in memory
    val svgBytes = ByteArrayOutputStream().use { baos ->
        ggsave(plot, baos, format = "svg")
        baos.toByteArray()
    }

    // 3. Convert SVG to iText elements
    return SvgConverter.convertToElements(ByteArrayInputStream(svgBytes))
//        val table = Tables.createRelativeWidthCols(2f, 1f, 1f, 1f, 1f, 1f)
//        table.addHeaderCell(Cells.createHeader("Treatment"))
//        table.addHeaderCell(Cells.createHeader("30-day survival"))
//        table.addHeaderCell(Cells.createHeader("90-day survival"))
//        table.addHeaderCell(Cells.createHeader("180-day survival"))
//        table.addHeaderCell(Cells.createHeader("360-day survival"))
//        table.addHeaderCell(Cells.createHeader("720-day survival"))
//        
//        val entries = SurvivalPredictionEntryFactory.create(survivalPredictions)
//        entries.forEach { entry ->
//            table.addCell(Cells.createContent(entry.treatment))
//            table.addCell(Cells.createContent(formatSurvivalPercentage(entry.day30SurvivalProbability)))
//            table.addCell(Cells.createContent(formatSurvivalPercentage(entry.day90SurvivalProbability)))
//            table.addCell(Cells.createContent(formatSurvivalPercentage(entry.day180SurvivalProbability)))
//            table.addCell(Cells.createContent(formatSurvivalPercentage(entry.day360SurvivalProbability)))
//            table.addCell(Cells.createContent(formatSurvivalPercentage(entry.day720SurvivalProbability)))
//            
//        }
//        return table
    }
    
    private fun formatSurvivalPercentage(optionalPercentage: Double?) : String {
        return optionalPercentage?.let { Formats.percentage(optionalPercentage) } ?: "N/A"
    }
}
