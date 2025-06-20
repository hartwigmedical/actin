package com.hartwig.actin.report.pdf.tables.soc

import com.hartwig.actin.report.interpretation.soc.SurvivalPredictionEntryFactory
import com.hartwig.actin.report.pdf.tables.TableGenerator
import com.hartwig.actin.report.pdf.util.Cells
import com.hartwig.actin.report.pdf.util.Formats
import com.hartwig.actin.report.pdf.util.Tables
import com.itextpdf.layout.element.Table

class SurvivalPredictionPerTreatmentGenerator(private val survivalPredictions: Map<String, List<Double>>) :
    TableGenerator {

    override fun title(): String {
        return "Survival predictions per treatment"
    }

    override fun forceKeepTogether(): Boolean {
        return true
    }

    override fun contents(): Table {
        val table = Tables.createRelativeWidthCols(2f, 1f, 1f, 1f, 1f, 1f)
        table.addHeaderCell(Cells.createHeader("Treatment"))
        table.addHeaderCell(Cells.createHeader("30-day survival"))
        table.addHeaderCell(Cells.createHeader("90-day survival"))
        table.addHeaderCell(Cells.createHeader("180-day survival"))
        table.addHeaderCell(Cells.createHeader("360-day survival"))
        table.addHeaderCell(Cells.createHeader("720-day survival"))
        
        val entries = SurvivalPredictionEntryFactory.create(survivalPredictions)
        entries.forEach { entry ->
            table.addCell(Cells.createContent(entry.treatment))
            table.addCell(Cells.createContent(formatSurvivalPercentage(entry.day30SurvivalProbability)))
            table.addCell(Cells.createContent(formatSurvivalPercentage(entry.day90SurvivalProbability)))
            table.addCell(Cells.createContent(formatSurvivalPercentage(entry.day180SurvivalProbability)))
            table.addCell(Cells.createContent(formatSurvivalPercentage(entry.day360SurvivalProbability)))
            table.addCell(Cells.createContent(formatSurvivalPercentage(entry.day720SurvivalProbability)))
            
        }
        return table
    }
    
    private fun formatSurvivalPercentage(optionalPercentage: Double?) : String {
        return optionalPercentage?.let { Formats.percentage(optionalPercentage) } ?: "N/A"
    }
}
