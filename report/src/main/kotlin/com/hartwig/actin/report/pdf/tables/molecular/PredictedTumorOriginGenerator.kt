package com.hartwig.actin.report.pdf.tables.molecular

import com.hartwig.actin.datamodel.molecular.MolecularTest
import com.hartwig.actin.datamodel.molecular.characteristics.CupPrediction
import com.hartwig.actin.datamodel.molecular.characteristics.CuppaMode
import com.hartwig.actin.molecular.interpretation.TumorOriginInterpreter
import com.hartwig.actin.report.pdf.ReportLabels
import com.hartwig.actin.report.pdf.tables.TableGenerator
import com.hartwig.actin.report.pdf.util.Cells
import com.hartwig.actin.report.pdf.util.Formats
import com.hartwig.actin.report.pdf.util.Styles
import com.hartwig.actin.report.pdf.util.Tables
import com.itextpdf.layout.element.Table

private const val ADDITIONAL_EMPTY_COLS = 1
private const val PADDING_LEFT = 20
private const val PADDING_RIGHT = 25

class PredictedTumorOriginGenerator(private val molecular: MolecularTest, private val labels: ReportLabels) : TableGenerator {

    override fun title(): String {
        return if (isWGTS()) labels.molecular.originTitleWgts() else labels.molecular.originTitle()
    }

    override fun forceKeepTogether(): Boolean {
        return true
    }

    override fun contents(): Table {
        val predictedTumorOrigin = molecular.characteristics.predictedTumorOrigin
        val tumorOriginInterpreter = TumorOriginInterpreter.create(molecular)
        val predictions = tumorOriginInterpreter.topPredictionsToDisplay()
        return if (predictions.isEmpty()) {
            val message = if (predictedTumorOrigin == null) Formats.VALUE_UNKNOWN else labels.molecular.originAllBelow10(
                Formats.percentage(predictedTumorOrigin.likelihood()),
                predictedTumorOrigin.cancerType()
            )
            Tables.createSingleCol().addCell(Cells.createContentNoBorder(message))
        } else {
            val standardColumnRelativeWidth = 3.5f
            val predictionColumnsRelativeWidths = FloatArray(predictions.size) { 2f }
            val emptyColumnRelativeWidth = 4f

            val table = Tables.createRelativeWidthCols(standardColumnRelativeWidth, *predictionColumnsRelativeWidths, emptyColumnRelativeWidth)
            table.addHeaderCell(Cells.createEmpty())
            predictions.indices.asSequence()
                .map { i: Int -> "${i + 1}. ${predictions[i].cancerType}" }
                .map { Cells.createHeader(it).setPaddingLeft(PADDING_LEFT.toFloat()) }
                .forEach(table::addHeaderCell)
            repeat(ADDITIONAL_EMPTY_COLS) { table.addHeaderCell(Cells.createEmpty()) }

            table.addCell(Cells.createContentBold(labels.molecular.originCombinedScore()))
            predictions.map {
                val likelihoodCell = Cells.createContentBold(Formats.percentage(it.likelihood)).setPaddingLeft(PADDING_LEFT.toFloat())
                if (!tumorOriginInterpreter.hasConfidentPrediction()) {
                    likelihoodCell.addStyle(Styles.tableNoticeStyle())
                }
                likelihoodCell
            }.forEach(table::addCell)
            repeat(ADDITIONAL_EMPTY_COLS) { table.addCell(Cells.createEmpty()) }

            table.addCell(Cells.createContent(labels.molecular.originScoreNote()))
            repeat(predictions.size) { table.addCell(Cells.createContent("")) }
            repeat(ADDITIONAL_EMPTY_COLS) { table.addCell(Cells.createEmpty()) }
            addClassifierRow(labels.molecular.originSnvTypes(), predictions, CupPrediction::snvPairwiseClassifier, table)
            addClassifierRow(
                labels.molecular.originSnvGenomic(), predictions, CupPrediction::genomicPositionClassifier, table
            )
            addClassifierRow(
                labels.molecular.originDriverGenes(), predictions, CupPrediction::featureClassifier, table
            )
            if (isWGTS()) {
                addClassifierRow(
                    labels.molecular.originGeneExpression(), predictions, CupPrediction::expressionPairWiseClassifier, table
                )
                addClassifierRow(
                    labels.molecular.originAltSplice(), predictions, CupPrediction::altSjCohortClassifier, table
                )
            }
            table.addCell(
                Cells.createSpanningSubNote(
                    labels.molecular.originOtherCohorts(
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
        table.addCell(Cells.createContent(classifierText).setPaddingLeft(PADDING_LEFT.toFloat()))
        predictions
            .asSequence()
            .map(classifierFunction)
            .map { it?.let(Formats::percentage) ?: labels.misc.notAvailable() }
            .map { Cells.createContent(it).setPaddingLeft(PADDING_LEFT.toFloat()).setPaddingRight(PADDING_RIGHT.toFloat()) }
            .forEach(table::addCell)
        repeat(ADDITIONAL_EMPTY_COLS) { table.addCell(Cells.createEmpty()) }
    }

    private fun isWGTS(): Boolean {
        return molecular.characteristics.predictedTumorOrigin?.cuppaMode() == CuppaMode.WGTS
    }
}
