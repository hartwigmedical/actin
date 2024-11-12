package com.hartwig.actin.report.pdf.tables.molecular

import com.hartwig.actin.datamodel.molecular.MolecularRecord
import com.hartwig.actin.datamodel.molecular.orange.characteristics.CupPrediction
import com.hartwig.actin.report.interpretation.TumorOriginInterpreter
import com.hartwig.actin.report.pdf.tables.TableGenerator
import com.hartwig.actin.report.pdf.util.Cells
import com.hartwig.actin.report.pdf.util.Formats
import com.hartwig.actin.report.pdf.util.Styles
import com.hartwig.actin.report.pdf.util.Tables
import com.itextpdf.layout.element.Table

class PredictedTumorOriginGenerator(private val molecular: MolecularRecord, private val width: Float) : TableGenerator {

    override fun title(): String {
        return "Predicted tumor origin"
    }

    override fun contents(): Table {
        val predictedTumorOrigin = molecular.characteristics.predictedTumorOrigin
        val tumorOriginInterpreter = TumorOriginInterpreter(predictedTumorOrigin)
        val predictions = tumorOriginInterpreter.topPredictions()
        return if (predictions.isEmpty()) {
            val message = if (predictedTumorOrigin == null) Formats.VALUE_UNKNOWN else String.format(
                "All tumor cohorts have a prediction lower than 10%%. Highest prediction: %s (%s)",
                predictedTumorOrigin.cancerType(),
                Formats.percentage(predictedTumorOrigin.likelihood())
            )
            Tables.createSingleColWithWidth(width).addCell(Cells.createContentNoBorder(message))
        } else {
            val numColumns = predictions.size + 1
            val table = Table(numColumns)
            table.addHeaderCell(Cells.createEmpty())
            predictions.indices.asSequence()
                .map { i: Int -> "${i + 1}. ${predictions[i].cancerType}" }
                .map { Cells.createHeader(it).setPaddingLeft(PADDING_LEFT.toFloat()) }
                .forEach(table::addHeaderCell)

            table.addCell(Cells.createContentBold("Combined prediction score"))
            predictions.map {
                val likelihoodCell = Cells.createContentBold(Formats.percentage(it.likelihood)).setPaddingLeft(PADDING_LEFT.toFloat())
                if (!tumorOriginInterpreter.hasConfidentPrediction()) {
                    likelihoodCell.addStyle(Styles.tableNoticeStyle())
                }
                likelihoodCell
            }.forEach(table::addCell)

            table.addCell(Cells.createContent("This score is calculated by combining information on:"))
            repeat(predictions.size) { table.addCell(Cells.createContent("")) }
            addClassifierRow("(1) SNV types", predictions, CupPrediction::snvPairwiseClassifier, table)
            addClassifierRow(
                "(2) SNV genomic localisation distribution", predictions, CupPrediction::genomicPositionClassifier, table
            )
            addClassifierRow(
                "(3) Driver genes and passenger characteristics", predictions, CupPrediction::featureClassifier, table
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

    companion object {
        private const val PADDING_LEFT = 20
        private const val PADDING_RIGHT = 25

        private fun addClassifierRow(
            classifierText: String, predictions: List<CupPrediction>,
            classifierFunction: (CupPrediction) -> Double, table: Table
        ) {
            table.addCell(Cells.createContent(classifierText).setPaddingLeft(PADDING_LEFT.toFloat()))
            predictions
                .asSequence()
                .map(classifierFunction)
                .map(Formats::percentage)
                .map { Cells.createContent(it).setPaddingLeft(PADDING_LEFT.toFloat()).setPaddingRight(PADDING_RIGHT.toFloat()) }
                .forEach(table::addCell)
        }
    }
}