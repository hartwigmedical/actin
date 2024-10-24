package com.hartwig.actin.report.pdf.tables.molecular

import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.molecular.Driver
import com.hartwig.actin.datamodel.molecular.DriverLikelihood
import com.hartwig.actin.datamodel.molecular.MolecularRecord
import com.hartwig.actin.datamodel.molecular.MolecularTest
import com.hartwig.actin.datamodel.molecular.orange.driver.CopyNumber
import com.hartwig.actin.report.interpretation.MolecularDriversSummarizer
import com.hartwig.actin.report.interpretation.TumorOriginInterpreter
import com.hartwig.actin.report.pdf.util.Cells
import com.hartwig.actin.report.pdf.util.Formats
import com.hartwig.actin.report.pdf.util.Formats.date
import com.hartwig.actin.report.pdf.util.Styles
import com.hartwig.actin.report.pdf.util.Tables
import com.itextpdf.layout.element.Cell
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Table
import com.itextpdf.layout.element.Text

object WGSSummaryGeneratorFunctions {

    fun createMolecularSummaryTitle(molecular: MolecularTest, isIncludedInTrialMatching: Boolean = false): String {
        return "${molecular.testTypeDisplay ?: molecular.experimentType.display()} (${if (isIncludedInTrialMatching) date(molecular.date) else "${molecular.date} - Test not included in trial matching as test age exceeds cutoff."})"
    }

    fun createMolecularSummaryTable(
        isShort: Boolean,
        patientRecord: PatientRecord,
        molecular: MolecularTest,
        wgsMolecular: MolecularRecord?,
        keyWidth: Float,
        valueWidth: Float,
        summarizer: MolecularDriversSummarizer
    ): Table {
        val table = Tables.createFixedWidthCols(keyWidth, valueWidth)

        if (!isShort) {
            table.addCell(Cells.createKey("Biopsy location"))
            table.addCell(biopsySummary(patientRecord, molecular))
        }

        if (wgsMolecular?.hasSufficientQuality != false) {
            if (!isShort) {
                table.addCell(Cells.createKey("Molecular tissue of origin prediction"))
                table.addCell(tumorOriginPredictionCell(molecular))
            }

            val hasTmbData = createTmbCells(molecular, isShort, table)

            val tableContents = generateTableContents(isShort, summarizer, molecular, keyWidth, valueWidth)

            val filteredContents = tableContents
                .filterNot { (_, value) -> (value.contains(Formats.VALUE_NONE) || value.contains(Formats.VALUE_UNKNOWN)) && isShort }
                .flatMap { (key, value) -> listOf(Cells.createKey(key), Cells.createValue(value)) }
            if (filteredContents.isNotEmpty() || hasTmbData) {
                filteredContents.forEach(table::addCell)
            } else {
                table.addCell(
                    Cells.createSpanningContent(
                        "No mutations found",
                        table
                    )
                )
            }
            val (actionableEventsWithUnknownDriver, actionableEventsWithLowOrMediumDriver) = summarizer.actionableEventsThatAreNotKeyDrivers()
                .partition { it.driverLikelihood == null }

            if (actionableEventsWithLowOrMediumDriver.isNotEmpty() || !isShort) {
                table.addCell(Cells.createKey("Potentially actionable events with medium/low driver:"))
                table.addCell(potentiallyActionableEventsCell(actionableEventsWithLowOrMediumDriver))
            }
            if (actionableEventsWithUnknownDriver.isNotEmpty()) {
                table.addCell(Cells.createKey("Potentially actionable events not considered drivers:"))
                table.addCell(potentiallyActionableEventsCell(actionableEventsWithUnknownDriver))
            }
        } else {
            table.addCell(
                Cells.createSpanningContent(
                    "The received biomaterial(s) did not meet the requirements that are needed for high quality whole genome sequencing",
                    table
                )
            )
        }
        return table
    }

    private fun createTmbCells(
        molecular: MolecularTest,
        isShort: Boolean,
        table: Table
    ): Boolean {
        val tmbStatus = tumorMutationalLoadAndTumorMutationalBurdenStatus(molecular)
        if (!isShort || tmbStatus != "${Formats.VALUE_UNKNOWN} / ${Formats.VALUE_UNKNOWN}") {
            table.addCell(Cells.createKey("Tumor mutational load / burden"))
            table.addCell(
                tumorMutationalLoadAndTumorMutationalBurdenStatusCell(
                    molecular,
                    tmbStatus
                )
            )
            return true
        }
        return false
    }

    private fun biopsySummary(patientRecord: PatientRecord, molecular: MolecularTest): Cell {
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

    private fun tumorOriginPredictionCell(molecular: MolecularTest): Cell {
        val wgsMolecular = if (molecular is MolecularRecord) molecular else null
        val paragraph = Paragraph(Text(tumorOriginPrediction(molecular, wgsMolecular)).addStyle(Styles.tableHighlightStyle()))
        val purity = molecular.characteristics.purity
        if (wgsMolecular != null && purity != null && wgsMolecular.hasSufficientQualityButLowPurity()) {
            val purityText = Text(" (low purity)").addStyle(Styles.tableNoticeStyle())
            paragraph.add(purityText)
        }
        return Cells.create(paragraph)
    }

    private fun tumorOriginPrediction(molecular: MolecularTest, wgsMolecular: MolecularRecord?): String {
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

    private fun tumorMutationalLoadAndTumorMutationalBurdenStatusCell(molecular: MolecularTest, status: String): Cell {
        val paragraph = Paragraph(Text(status).addStyle(Styles.tableHighlightStyle()))
        val purity = molecular.characteristics.purity
        val wgsMolecular = if (molecular is MolecularRecord) molecular else null
        if (wgsMolecular != null && purity != null && wgsMolecular.hasSufficientQualityButLowPurity()) {
            val purityText = Text(" (low purity)").addStyle(Styles.tableNoticeStyle())
            paragraph.add(purityText)
        }
        return Cells.create(paragraph)
    }

    fun potentiallyActionableEventsCell(drivers: List<Driver>): Cell {
        if (drivers.isEmpty()) return Cells.createValue(Formats.VALUE_NONE)

        val eventText = drivers.distinctBy(Driver::event).flatMap { driver ->
            val warning = when (driver.driverLikelihood) {
                DriverLikelihood.LOW -> " (low driver likelihood)"
                DriverLikelihood.MEDIUM -> " (medium driver likelihood)"
                else -> if (driver is CopyNumber) "" else " (dubious quality)"
            }
            listOf(
                Text(driver.event).addStyle(Styles.tableHighlightStyle()),
                Text(warning).addStyle(Styles.tableNoticeStyle()),
                Text(", ").addStyle(Styles.tableHighlightStyle()),
            )
        }.dropLast(1)
        val paragraph = Paragraph().addAll(eventText)

        return Cells.create(paragraph)
    }

    private fun tumorMutationalLoadAndTumorMutationalBurdenStatus(molecular: MolecularTest): String {
        val hasHighTumorMutationalLoad = molecular.characteristics.hasHighTumorMutationalLoad
        val tumorMutationalLoad = molecular.characteristics.tumorMutationalLoad
        val tmlString = if (tumorMutationalLoad == null || hasHighTumorMutationalLoad == null) Formats.VALUE_UNKNOWN else String.format(
            "TML %s (%d)",
            if (hasHighTumorMutationalLoad) "high" else "low",
            tumorMutationalLoad
        )
        val hasHighTumorMutationalBurden = molecular.characteristics.hasHighTumorMutationalBurden
        val tumorMutationalBurden = molecular.characteristics.tumorMutationalBurden
        val tmbString = if (tumorMutationalBurden == null || hasHighTumorMutationalBurden == null) Formats.VALUE_UNKNOWN else String.format(
            "TMB %s (%s)",
            if (hasHighTumorMutationalBurden) "high" else "low",
            Formats.singleDigitNumber(tumorMutationalBurden)
        )
        return String.format("%s / %s", tmlString, tmbString)
    }

    private fun formatList(list: List<String>): String {
        return if (list.isEmpty()) Formats.VALUE_NONE else list.joinToString(Formats.COMMA_SEPARATOR)
    }

    private fun getOrderedKeys(isShort: Boolean): List<String> {
        return if (isShort) {
            listOf(
                "High driver mutations",
                "Amplified genes",
                "Gene fusions",
                "Deleted genes",
                "Homozygously disrupted genes",
                "Microsatellite (in)stability",
                "",
            )
        } else {
            listOf(
                "Microsatellite (in)stability",
                "HR status",
                "",
                "High driver mutations",
                "Amplified genes",
                "Deleted genes",
                "Homozygously disrupted genes",
                "Gene fusions",
                "Virus detection",
                "",
            )
        }
    }

    private fun generateTableContents(
        isShort: Boolean, summarizer: MolecularDriversSummarizer, molecular: MolecularTest, keyWidth: Float, valueWidth: Float
    ): List<Pair<String, String>> {
        val characteristicsGenerator = MolecularCharacteristicsGenerator(molecular, keyWidth + valueWidth)
        val orderedKeys = getOrderedKeys(isShort)
        val keyToValueMap = mapOf(
            "Microsatellite (in)stability" to (characteristicsGenerator.createMSStabilityString() ?: Formats.VALUE_UNKNOWN),
            "HR status" to (characteristicsGenerator.createHRStatusString() ?: Formats.VALUE_UNKNOWN),
            "High driver mutations" to formatList(summarizer.keyVariants()),
            "Amplified genes" to formatList(summarizer.keyAmplifiedGenes()),
            "Deleted genes" to formatList(summarizer.keyDeletedGenes()),
            "Homozygously disrupted genes" to formatList(summarizer.keyHomozygouslyDisruptedGenes()),
            "Gene fusions" to formatList(summarizer.keyFusionEvents()),
            "Virus detection" to formatList(summarizer.keyVirusEvents()),
        )
        return orderedKeys.mapNotNull { key -> keyToValueMap[key]?.let { value -> key to value } }
    }
}