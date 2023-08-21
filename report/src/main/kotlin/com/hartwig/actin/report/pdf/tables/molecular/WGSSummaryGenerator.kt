package com.hartwig.actin.report.pdf.tables.molecular

import com.google.common.collect.Maps
import com.hartwig.actin.clinical.datamodel.ClinicalRecord
import com.hartwig.actin.molecular.datamodel.MolecularRecord
import com.hartwig.actin.molecular.datamodel.characteristics.CuppaPrediction
import com.hartwig.actin.report.interpretation.EvaluatedCohort
import com.hartwig.actin.report.interpretation.MolecularDriversSummarizer
import com.hartwig.actin.report.interpretation.TumorOriginInterpreter
import com.hartwig.actin.report.pdf.tables.TableGenerator
import com.hartwig.actin.report.pdf.util.Cells
import com.hartwig.actin.report.pdf.util.Formats
import com.hartwig.actin.report.pdf.util.Styles
import com.hartwig.actin.report.pdf.util.Tables
import com.hartwig.actin.util.ApplicationConfig
import com.itextpdf.layout.element.Cell
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Table
import com.itextpdf.layout.element.Text
import java.util.*
import java.util.function.Function
import java.util.stream.Collectors
import java.util.stream.Stream

class WGSSummaryGenerator(
    private val clinical: ClinicalRecord, private val molecular: MolecularRecord,
    cohorts: List<EvaluatedCohort?>, private val keyWidth: Float, private val valueWidth: Float
) : TableGenerator {
    private val summarizer: MolecularDriversSummarizer

    init {
        summarizer = MolecularDriversSummarizer.Companion.fromMolecularDriversAndEvaluatedCohorts(molecular.drivers(), cohorts)
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
            Stream.of(
                Maps.immutableEntry("Tumor mutational load / burden", characteristicsGenerator.createTMLAndTMBStatusString()),
                Maps.immutableEntry(
                    "Microsatellite (in)stability",
                    characteristicsGenerator.createMSStabilityStringOption().orElse(Formats.VALUE_UNKNOWN)
                ),
                Maps.immutableEntry("HR status", characteristicsGenerator.createHRStatusStringOption().orElse(Formats.VALUE_UNKNOWN)),
                Maps.immutableEntry("", ""),
                Maps.immutableEntry("Genes with high driver mutation", formatStream(summarizer.keyGenesWithVariants())),
                Maps.immutableEntry("Amplified genes", formatStream(summarizer.keyAmplifiedGenes())),
                Maps.immutableEntry("Deleted genes", formatStream(summarizer.keyDeletedGenes())),
                Maps.immutableEntry("Homozygously disrupted genes", formatStream(summarizer.keyHomozygouslyDisruptedGenes())),
                Maps.immutableEntry("Gene fusions", formatStream(summarizer.keyFusionEvents())),
                Maps.immutableEntry("Virus detection", formatStream(summarizer.keyVirusEvents())),
                Maps.immutableEntry("", ""),
                Maps.immutableEntry(
                    "Potentially actionable events with medium/low driver:",
                    formatStream(summarizer.actionableEventsThatAreNotKeyDrivers())
                )
            )
                .flatMap<Cell>(Function<Map.Entry<String, String>, Stream<out Cell>> { (key, value): Map.Entry<String, String> ->
                    Stream.of(
                        Cells.createKey(
                            key
                        ), Cells.createValue(value)
                    )
                })
                .forEach { cell: Cell? -> table.addCell(cell) }
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
            Cells.create(
                Paragraph().addAll(
                    Arrays.asList(
                        biopsyText,
                        purityText
                    )
                )
            )
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
                String.format("Inconclusive (%s)",
                    predictionsMeetingThreshold.stream()
                        .map { prediction: CuppaPrediction? ->
                            String.format(
                                "%s %s",
                                prediction!!.cancerType(),
                                Formats.percentage(prediction.likelihood())
                            )
                        }
                        .collect(Collectors.joining(", ")))
            }
        } else {
            Formats.VALUE_UNKNOWN
        }
    }

    private fun formatStream(stream: Stream<String?>?): String {
        val collected = stream!!.collect(Collectors.joining(Formats.COMMA_SEPARATOR))
        return if (collected.isEmpty()) Formats.VALUE_NONE else collected
    }
}