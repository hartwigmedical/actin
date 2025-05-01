package com.hartwig.actin.report.pdf.tables.molecular

import com.hartwig.actin.datamodel.molecular.MolecularRecord
import com.hartwig.actin.datamodel.molecular.characteristics.CupPrediction
import com.hartwig.actin.datamodel.molecular.characteristics.CuppaMode
import com.hartwig.actin.report.interpretation.TumorOriginInterpreter
import com.hartwig.actin.report.pdf.tables.TableGenerator
import com.hartwig.actin.report.pdf.util.Cells
import com.hartwig.actin.report.pdf.util.Formats
import com.hartwig.actin.report.pdf.util.Styles
import com.hartwig.actin.report.pdf.util.Tables
import com.itextpdf.layout.element.Table

private const val ADDITIONAL_EMPTY_COLS = 2
private const val PADDING_LEFT = 20
private const val PADDING_RIGHT = 25

class PredictedTumorOriginGenerator(private val molecular: MolecularRecord) : TableGenerator {

    override fun title(): String {
        val cuppaModeIsWGTS = if (molecular.characteristics.predictedTumorOrigin?.cuppaMode() == CuppaMode.WGTS) " (WGTS)" else ""
        return "Predicted tumor origin${cuppaModeIsWGTS}"
    }

    override fun forceKeepTogether(): Boolean {
        return true
    }

    override fun contents(): Table {
        val predictedTumorOrigin = molecular.characteristics.predictedTumorOrigin
        val tumorOriginInterpreter = TumorOriginInterpreter.create(molecular)
        val predictions = tumorOriginInterpreter.topPredictionsToDisplay()
        return if (predictions.isEmpty()) {
            val message = if (predictedTumorOrigin == null) Formats.VALUE_UNKNOWN else String.format(
                "All tumor cohorts have a prediction lower than 10%%. Highest prediction: %s (%s)",
                predictedTumorOrigin.cancerType(),
                Formats.percentage(predictedTumorOrigin.likelihood())
            )
            Tables.createSingleCol().addCell(Cells.createContentNoBorder(message))
        } else {
            val numColumns = predictions.size + 1 + ADDITIONAL_EMPTY_COLS
            val table = Tables.createMultiCol(numColumns)
            table.addHeaderCell(Cells.createEmpty())
            predictions.indices.asSequence()
                .map { i: Int -> "${i + 1}. ${predictions[i].cancerType}" }
                .map { Cells.createHeader(it).setPaddingLeft(PADDING_LEFT.toFloat()) }
                .forEach(table::addHeaderCell)
            repeat(ADDITIONAL_EMPTY_COLS) { table.addHeaderCell(Cells.createEmpty()) }

            table.addCell(Cells.createContentBold("Combined prediction score"))
            predictions.map {
                val likelihoodCell = Cells.createContentBold(Formats.percentage(it.likelihood)).setPaddingLeft(PADDING_LEFT.toFloat())
                if (!tumorOriginInterpreter.hasConfidentPrediction()) {
                    likelihoodCell.addStyle(Styles.tableNoticeStyle())
                }
                likelihoodCell
            }.forEach(table::addCell)
            repeat(ADDITIONAL_EMPTY_COLS) { table.addCell(Cells.createEmpty()) }
            
            table.addCell(Cells.createContent("This score is calculated by combining information on:"))
            repeat(predictions.size) { table.addCell(Cells.createContent("")) }
            repeat(ADDITIONAL_EMPTY_COLS) { table.addCell(Cells.createEmpty()) }
            addClassifierRow("(1) SNV types", predictions, CupPrediction::snvPairwiseClassifier, table)
            addClassifierRow(
                "(2) SNV genomic localisation distribution", predictions, CupPrediction::genomicPositionClassifier, table
            )
            addClassifierRow(
                "(3) Driver genes and passenger characteristics", predictions, CupPrediction::featureClassifier, table
            )
            addClassifierRow(
                "(4) SNV genomic localisation distribution", predictions, CupPrediction::expressionPairWiseClassifier, table
            )
            addClassifierRow(
                "(5) Driver genes and passenger characteristics", predictions, CupPrediction::altSjCohortClassifier, table
            )
            table.addCell(
                Cells.createSpanningSubNote(
                    String.format(
                        "Other cohorts have a combined prediction of %s or lower",
                        Formats.percentage(tumorOriginInterpreter.greatestOmittedLikelihood())
                    ), table
                )
            )
            table
        }
    }

    private fun addClassifierRow(
        classifierText: String, predictions: List<CupPrediction>, classifierFunction: (CupPrediction) -> Double?, table: Table
    ) {
        if (predictions.map(classifierFunction).all { it == null })
            return

        table.addCell(Cells.createContent(classifierText).setPaddingLeft(PADDING_LEFT.toFloat()))
        predictions
            .asSequence()
            .map(classifierFunction)
            .map {
                if(it == null){
                    "N/A"
                } else {
                    Formats.percentage(it)
                }
            }
            .map { Cells.createContent(it).setPaddingLeft(PADDING_LEFT.toFloat()).setPaddingRight(PADDING_RIGHT.toFloat()) }
            .forEach(table::addCell)
        repeat(ADDITIONAL_EMPTY_COLS) { table.addCell(Cells.createEmpty()) }
    }
}