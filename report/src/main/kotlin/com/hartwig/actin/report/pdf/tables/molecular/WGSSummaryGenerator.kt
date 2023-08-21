package com.hartwig.actin.report.pdf.tables.molecular

import com.hartwig.actin.clinical.datamodel.ClinicalRecord
import com.hartwig.actin.molecular.datamodel.MolecularRecord
import com.hartwig.actin.report.interpretation.EvaluatedCohort
import com.hartwig.actin.report.interpretation.MolecularDriversSummarizer
import com.hartwig.actin.report.interpretation.TumorOriginInterpreter
import com.hartwig.actin.report.pdf.tables.TableGenerator
import com.hartwig.actin.report.pdf.util.Cells
import com.hartwig.actin.report.pdf.util.Formats
import com.hartwig.actin.report.pdf.util.Formats.date
import com.hartwig.actin.report.pdf.util.Styles
import com.hartwig.actin.report.pdf.util.Tables
import com.hartwig.actin.util.ApplicationConfig
import com.itextpdf.layout.element.Cell
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Table
import com.itextpdf.layout.element.Text

class WGSSummaryGenerator(
    private val clinical: ClinicalRecord, private val molecular: MolecularRecord,
    cohorts: List<EvaluatedCohort>, private val keyWidth: Float, private val valueWidth: Float
) : TableGenerator {
    private val summarizer: MolecularDriversSummarizer

    init {
        summarizer = MolecularDriversSummarizer.fromMolecularDriversAndEvaluatedCohorts(molecular.drivers(), cohorts)
    }

    override fun title(): String {
        return String.format(
            ApplicationConfig.LOCALE,
            "%s of %s (%s)",
            molecular.type(),
            clinical.patientId(),
            date(molecular.date())
        )
    }

    override fun contents(): Table {
        val characteristicsGenerator = MolecularCharacteristicsGenerator(molecular, keyWidth + valueWidth)
        val table = Tables.createFixedWidthCols(keyWidth, valueWidth)
        table.addCell(Cells.createKey("Biopsy location"))
        table.addCell(biopsySummary())
        if (molecular.containsTumorCells()) {
            table.addCell(Cells.createKey("Molecular tissue of origin prediction"))
            table.addCell(tumorOriginPredictionCell())
            listOf(
                "Tumor mutational load / burden" to characteristicsGenerator.createTMLAndTMBStatusString(),
                "Microsatellite (in)stability" to (characteristicsGenerator.createMSStabilityString() ?: Formats.VALUE_UNKNOWN),
                "HR status" to (characteristicsGenerator.createHRStatusString() ?: Formats.VALUE_UNKNOWN),
                "" to "",
                "Genes with high driver mutation" to formatList(summarizer.keyGenesWithVariants()),
                "Amplified genes" to formatList(summarizer.keyAmplifiedGenes()),
                "Deleted genes" to formatList(summarizer.keyDeletedGenes()),
                "Homozygously disrupted genes" to formatList(summarizer.keyHomozygouslyDisruptedGenes()),
                "Gene fusions" to formatList(summarizer.keyFusionEvents()),
                "Virus detection" to formatList(summarizer.keyVirusEvents()),
                "" to "",
                "Potentially actionable events with medium/low driver:" to formatList(summarizer.actionableEventsThatAreNotKeyDrivers())
            )
                .flatMap { (key, value) -> listOf(Cells.createKey(key), Cells.createValue(value)) }
                .forEach(table::addCell)
        } else {
            table.addCell(
                Cells.createSpanningEntry(
                    "The received biomaterial(s) did not meet the requirements that are needed for "
                            + "high quality whole genome sequencing", table
                )
            )
        }
        return table
    }

    private fun biopsySummary(): Cell {
        val biopsyLocation = if (clinical.tumor().biopsyLocation() != null) clinical.tumor().biopsyLocation() else Formats.VALUE_UNKNOWN
        val purity = molecular.characteristics().purity()
        return if (purity != null) {
            val biopsyText = Text(biopsyLocation).addStyle(Styles.tableHighlightStyle())
            val purityText = Text(String.format(" (purity %s)", Formats.percentage(purity)))
            purityText.addStyle(if (molecular.hasSufficientQualityAndPurity()) Styles.tableHighlightStyle() else Styles.tableNoticeStyle())
            Cells.create(Paragraph().addAll(listOf(biopsyText, purityText)))
        } else {
            Cells.createValue(biopsyLocation!!)
        }
    }

    private fun tumorOriginPredictionCell(): Cell {
        val paragraph = Paragraph(Text(tumorOriginPrediction()).addStyle(Styles.tableHighlightStyle()))
        val purity = molecular.characteristics().purity()
        if (purity != null && purity < 0.2) {
            val purityText = Text(String.format(" (purity %s)", Formats.percentage(purity))).addStyle(Styles.tableNoticeStyle())
            paragraph.add(purityText)
        }
        return Cells.create(paragraph)
    }

    private fun tumorOriginPrediction(): String {
        val predictedTumorOrigin = molecular.characteristics().predictedTumorOrigin()
        return if (TumorOriginInterpreter.hasConfidentPrediction(predictedTumorOrigin) && molecular.hasSufficientQualityAndPurity()) {
            TumorOriginInterpreter.interpret(predictedTumorOrigin)
        } else if (molecular.hasSufficientQuality() && predictedTumorOrigin != null) {
            val predictionsMeetingThreshold = TumorOriginInterpreter.predictionsToDisplay(predictedTumorOrigin)
            if (predictionsMeetingThreshold.isEmpty()) {
                String.format(
                    "Inconclusive (%s %s)",
                    predictedTumorOrigin.cancerType(),
                    Formats.percentage(predictedTumorOrigin.likelihood())
                )
            } else {
                String.format("Inconclusive (%s)", predictionsMeetingThreshold.joinToString(", ") {
                    "${it.cancerType()} ${Formats.percentage(it.likelihood())}"
                })
            }
        } else {
            Formats.VALUE_UNKNOWN
        }
    }

    private fun formatList(list: List<String>): String {
        return if (list.isEmpty()) Formats.VALUE_NONE else list.joinToString(Formats.COMMA_SEPARATOR)
    }
}