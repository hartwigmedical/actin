package com.hartwig.actin.report.pdf.tables.molecular

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.molecular.datamodel.MolecularRecord
import com.hartwig.actin.molecular.datamodel.MolecularTest
import com.hartwig.actin.report.interpretation.TumorOriginInterpreter
import com.hartwig.actin.report.pdf.util.Cells
import com.hartwig.actin.report.pdf.util.Formats
import com.hartwig.actin.report.pdf.util.Styles
import com.itextpdf.layout.element.Cell
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Text

class WGSSummaryGeneratorFunctions(
    private val patientRecord: PatientRecord, private val molecular: MolecularTest, private val wgsMolecular: MolecularRecord?
) {

    internal fun biopsySummary(): Cell {
        val biopsyLocation = patientRecord.tumor.biopsyLocation ?: Formats.VALUE_UNKNOWN
        val purity = molecular.characteristics.purity
        val wgsMolecular = if (molecular is MolecularRecord) molecular else null
        return if (wgsMolecular != null && purity != null) {
            val biopsyText = Text(biopsyLocation).addStyle(Styles.tableHighlightStyle())
            val purityText = Text(String.format(" (purity %s)", Formats.percentage(purity)))
            purityText.addStyle(if (wgsMolecular.hasSufficientQualityButLowPurity()) Styles.tableNoticeStyle() else Styles.tableHighlightStyle())
            Cells.create(Paragraph().addAll(listOf(biopsyText, purityText)))
        } else {
            Cells.createValue(biopsyLocation)
        }
    }

    internal fun tumorOriginPredictionCell(): Cell {
        val paragraph = Paragraph(Text(tumorOriginPrediction()).addStyle(Styles.tableHighlightStyle()))
        val purity = molecular.characteristics.purity
        val wgsMolecular = if (molecular is MolecularRecord) molecular else null
        if (wgsMolecular != null && purity != null && wgsMolecular.hasSufficientQualityButLowPurity()) {
            val purityText = Text(" (low purity)").addStyle(Styles.tableNoticeStyle())
            paragraph.add(purityText)
        }
        return Cells.create(paragraph)
    }

    private fun tumorOriginPrediction(): String {
        val predictedTumorOrigin = molecular.characteristics.predictedTumorOrigin
        return if (TumorOriginInterpreter.hasConfidentPrediction(predictedTumorOrigin) && wgsMolecular?.hasSufficientQualityAndPurity() == true) {
            TumorOriginInterpreter.interpret(predictedTumorOrigin)
        } else if (wgsMolecular?.hasSufficientQuality == true && predictedTumorOrigin != null) {
            val predictionsMeetingThreshold = TumorOriginInterpreter.predictionsToDisplay(predictedTumorOrigin)
            if (predictionsMeetingThreshold.isEmpty()) {
                String.format(
                    "Inconclusive (%s %s)",
                    predictedTumorOrigin.cancerType(),
                    Formats.percentage(predictedTumorOrigin.likelihood())
                )
            } else {
                String.format("Inconclusive (%s)", predictionsMeetingThreshold.joinToString(", ") {
                    "${it.cancerType} ${Formats.percentage(it.likelihood)}"
                })
            }
        } else {
            Formats.VALUE_UNKNOWN
        }
    }

    internal fun tumorMutationalLoadAndTumorMutationalBurdenStatusCell(): Cell {
        val paragraph = Paragraph(Text(tumorMutationalLoadAndTumorMutationalBurdenStatus()).addStyle(Styles.tableHighlightStyle()))
        val purity = molecular.characteristics.purity
        val wgsMolecular = if (molecular is MolecularRecord) molecular else null
        if (wgsMolecular != null && purity != null && wgsMolecular.hasSufficientQualityButLowPurity()) {
            val purityText = Text(" (low purity)").addStyle(Styles.tableNoticeStyle())
            paragraph.add(purityText)
        }
        return Cells.create(paragraph)
    }

    private fun tumorMutationalLoadAndTumorMutationalBurdenStatus(): String {
        val hasHighTumorMutationalLoad = molecular.characteristics.hasHighTumorMutationalLoad
        val tumorMutationalLoad = molecular.characteristics.tumorMutationalLoad
        val TMLString = if (tumorMutationalLoad == null || hasHighTumorMutationalLoad == null) Formats.VALUE_UNKNOWN else String.format(
            "TML %s (%d)",
            if (hasHighTumorMutationalLoad) "high" else "low",
            tumorMutationalLoad
        )
        val hasHighTumorMutationalBurden = molecular.characteristics.hasHighTumorMutationalBurden
        val tumorMutationalBurden = molecular.characteristics.tumorMutationalBurden
        val TMBString = if (tumorMutationalBurden == null || hasHighTumorMutationalBurden == null) Formats.VALUE_UNKNOWN else String.format(
            "TMB %s (%s)",
            if (hasHighTumorMutationalBurden) "high" else "low",
            Formats.singleDigitNumber(tumorMutationalBurden)
        )
        return String.format("%s / %s", TMLString, TMBString)
    }
}