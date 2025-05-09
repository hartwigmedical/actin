package com.hartwig.actin.report.pdf.tables.molecular

import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.molecular.MolecularRecord
import com.hartwig.actin.datamodel.molecular.MolecularTest
import com.hartwig.actin.datamodel.molecular.characteristics.CuppaMode
import com.hartwig.actin.datamodel.molecular.driver.CopyNumber
import com.hartwig.actin.datamodel.molecular.driver.Driver
import com.hartwig.actin.datamodel.molecular.driver.DriverLikelihood
import com.hartwig.actin.report.interpretation.MolecularCharacteristicFormat
import com.hartwig.actin.report.interpretation.MolecularDriversSummarizer
import com.hartwig.actin.report.interpretation.TumorOriginInterpreter
import com.hartwig.actin.report.pdf.util.Cells
import com.hartwig.actin.report.pdf.util.Formats
import com.hartwig.actin.report.pdf.util.Styles
import com.hartwig.actin.report.pdf.util.Tables
import com.itextpdf.layout.element.Cell
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Table
import com.itextpdf.layout.element.Text

object WGSSummaryGeneratorFunctions {

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
                val cuppaModeIsWGTS = if (molecular.characteristics.predictedTumorOrigin?.cuppaMode() == CuppaMode.WGTS) " (WGTS)" else ""
                table.addCell(Cells.createKey("Molecular tissue of origin prediction${cuppaModeIsWGTS}"))
                table.addCell(tumorOriginPredictionCell(molecular))
            }

            val hasTmbData = createTmbCells(molecular, isShort, table)

            val tableContents = generateTableContents(isShort, summarizer, molecular)

            val filteredContents = tableContents
                .filterNot { (_, value) -> (value.contains(Formats.VALUE_NONE) || value.contains(Formats.VALUE_UNKNOWN)) && isShort }
                .flatMap { (key, value) -> listOf(Cells.createKey(key), Cells.createValue(value)) }
            if (filteredContents.isNotEmpty() || hasTmbData) {
                filteredContents.forEach(table::addCell)
            }

            val (actionableEventsWithUnknownDriver, actionableEventsWithLowOrMediumDriver) =
                summarizer.actionableEventsThatAreNotKeyDrivers().partition { it.driverLikelihood == null }
            val ploidy = molecular.characteristics.ploidy

            if (actionableEventsWithLowOrMediumDriver.isNotEmpty() || !isShort) {
                table.addCell(Cells.createKey("Trial-relevant events, considered medium/low driver:"))
                table.addCell(potentiallyActionableEventsCell(actionableEventsWithLowOrMediumDriver, ploidy))
            }
            if (actionableEventsWithUnknownDriver.isNotEmpty()) {
                table.addCell(Cells.createKey("Trial-relevant events, not considered a tumor driver:"))
                table.addCell(potentiallyActionableEventsCell(actionableEventsWithUnknownDriver, ploidy))
            }

            if (filteredContents.isEmpty() && !hasTmbData &&
                actionableEventsWithLowOrMediumDriver.isEmpty() && actionableEventsWithUnknownDriver.isEmpty()
            ) {
                table.addCell(Cells.createSpanningContent("No mutations found", table))
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

    fun createTmbCells(
        molecular: MolecularTest,
        isShort: Boolean,
        table: Table
    ): Boolean {
        val tmlUnknownAndTmbKnown =
            molecular.characteristics.tumorMutationalLoad == null && molecular.characteristics.tumorMutationalBurden != null
        val tmlAndTmbKnown =
            molecular.characteristics.tumorMutationalLoad != null && molecular.characteristics.tumorMutationalBurden != null
        if (tmlUnknownAndTmbKnown) {
            val tmbStatus = MolecularCharacteristicFormat.formatTumorMutationalBurden(molecular.characteristics, true)
            table.addCell(Cells.createKey("Tumor mutational burden"))
            table.addCell(tumorMutationalLoadAndTumorMutationalBurdenStatusCell(molecular, tmbStatus))
            return true
        } else if (!isShort || tmlAndTmbKnown) {
            val tmlAndTmbStatus = tumorMutationalLoadAndTumorMutationalBurdenStatus(molecular)
            table.addCell(Cells.createKey("Tumor mutational load / burden"))
            table.addCell(tumorMutationalLoadAndTumorMutationalBurdenStatusCell(molecular, tmlAndTmbStatus))
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
            purityText.addStyle(
                if (wgsMolecular.hasSufficientQualityButLowPurity()) Styles.tableNoticeStyle() else Styles.tableHighlightStyle()
            )
            Cells.create(Paragraph().addAll(listOf(biopsyText, purityText)))
        } else {
            Cells.createValue(biopsyLocation)
        }
    }

    fun tumorOriginPredictionCell(molecular: MolecularTest): Cell {
        val originSummary = TumorOriginInterpreter.create(molecular).generateSummaryString()

        val wgsMolecular = molecular as? MolecularRecord
        val paragraph = Paragraph(Text(originSummary).addStyle(Styles.tableHighlightStyle()))
        if (molecular.characteristics.purity != null && wgsMolecular?.hasSufficientQualityButLowPurity() == true) {
            paragraph.add(Text(" (low purity)").addStyle(Styles.tableNoticeStyle()))
        }
        return Cells.create(paragraph)
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

    fun potentiallyActionableEventsCell(drivers: List<Driver>, ploidy: Double? = null): Cell {
        if (drivers.isEmpty()) return Cells.createValue(Formats.VALUE_NONE)

        val eventText = drivers.distinctBy(Driver::event).flatMap { driver ->
            val driverLikelihoodText = " (${driver.driverLikelihood?.name?.lowercase()} driver likelihood)"
            val warning = when (driver.driverLikelihood) {
                DriverLikelihood.LOW -> driverLikelihoodText
                DriverLikelihood.MEDIUM -> driverLikelihoodText
                DriverLikelihood.HIGH -> ""
                null -> {
                    if (driver is CopyNumber) {
                        val ploidyString = ploidy?.let { " - with tumor ploidy $it)" } ?: ")"
                        " (${driver.canonicalImpact.minCopies} copies$ploidyString"
                    } else " (dubious quality)"
                }
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
        val tmlString = MolecularCharacteristicFormat.formatTumorMutationalLoad(molecular.characteristics, true)
        val tmbString = MolecularCharacteristicFormat.formatTumorMutationalBurden(molecular.characteristics, true)
        return String.format("%s / %s", tmlString, tmbString)
    }

    private fun formatList(list: List<String>): String {
        return if (list.isEmpty()) Formats.VALUE_NONE else list.joinToString(Formats.COMMA_SEPARATOR)
    }

    private fun generateTableContents(
        isShort: Boolean,
        summarizer: MolecularDriversSummarizer,
        molecular: MolecularTest
    ): List<Pair<String, String>> {
        val characteristicsGenerator = MolecularCharacteristicsGenerator(molecular)
        val orderedKeys = determineOrderedKeys(isShort)
        val keyToValueMap = mapOf(
            "Microsatellite (in)stability" to characteristicsGenerator.createMSStabilityString(),
            "HR status" to characteristicsGenerator.createHRStatusString(),
            "High driver mutations" to formatList(summarizer.keyVariants()),
            "Amplified genes" to formatList(summarizer.keyAmplifiedGenes()),
            "Deleted genes" to formatList(summarizer.keyDeletedGenes()),
            "Homozygously disrupted genes" to formatList(summarizer.keyHomozygouslyDisruptedGenes()),
            "Gene fusions" to formatList(summarizer.keyFusionEvents()),
            "Virus detection" to formatList(summarizer.keyVirusEvents()),
        )
        return orderedKeys.mapNotNull { key -> keyToValueMap[key]?.let { value -> key to value } }
    }

    private fun determineOrderedKeys(isShort: Boolean): List<String> {
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
}