package com.hartwig.actin.report.pdf.tables.molecular

import com.hartwig.actin.datamodel.PatientRecord
import com.hartwig.actin.datamodel.molecular.ExperimentType
import com.hartwig.actin.datamodel.molecular.MolecularHistory
import com.hartwig.actin.datamodel.molecular.MolecularTest
import com.hartwig.actin.datamodel.molecular.characteristics.CuppaMode
import com.hartwig.actin.datamodel.molecular.driver.CopyNumber
import com.hartwig.actin.datamodel.molecular.driver.Driver
import com.hartwig.actin.datamodel.molecular.driver.DriverLikelihood
import com.hartwig.actin.report.interpretation.DriverDisplayFunctions.eventDisplay
import com.hartwig.actin.report.interpretation.MolecularCharacteristicFormat
import com.hartwig.actin.report.interpretation.MolecularDriversSummarizer
import com.hartwig.actin.molecular.interpretation.TumorOriginInterpreter
import com.hartwig.actin.report.interpretation.generateSummaryString
import com.hartwig.actin.report.pdf.ReportLabels
import com.hartwig.actin.report.pdf.SummaryType
import com.hartwig.actin.report.pdf.util.Cells
import com.hartwig.actin.report.pdf.util.Formats
import com.hartwig.actin.report.pdf.util.Styles
import com.hartwig.actin.report.pdf.util.Tables
import com.itextpdf.layout.element.Cell
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Table
import com.itextpdf.layout.element.Text

object WgsSummaryGeneratorFunctions {

    fun createMolecularSummaryTable(
        summaryType: SummaryType,
        patientRecord: PatientRecord,
        molecular: MolecularTest,
        wgsMolecular: MolecularTest?,
        keyWidth: Float,
        valueWidth: Float,
        summarizer: MolecularDriversSummarizer,
        labels: ReportLabels,
        immunologyGenerator: ImmunologyGenerator? = null
    ): Table {
        val table = Tables.createFixedWidthCols(keyWidth, valueWidth)
        val isLongSummaryType = summaryType == SummaryType.LONG_SUMMARY
        val isDetailsSummaryType = summaryType == SummaryType.DETAILS

        if (isDetailsSummaryType && molecular.targetSpecification?.testVersion?.testDateIsBeforeOldestTestVersion == true) {
            table.addCell(
                Cells.createSpanningSubNote(
                    labels.molecularOldTestVersion(
                        molecular.date.toString(),
                        molecular.targetSpecification?.testVersion?.versionDate!!.toString()
                    ),
                    table
                )
            )
        }

        if (wgsMolecular?.hasSufficientQuality != false) {
            if (isLongSummaryType || (isDetailsSummaryType && wgsMolecular?.characteristics?.predictedTumorOrigin != null)) {
                val tissueOriginTitle = if (molecular.characteristics.predictedTumorOrigin?.cuppaMode() == CuppaMode.WGTS)
                    labels.molecularWgsTissueOriginTitleWgts() else labels.molecularWgsTissueOriginTitle()
                table.addCell(Cells.createKey(tissueOriginTitle))
                table.addCell(tumorOriginPredictionCell(molecular))
            }

            val hasTmbData = createTmbCells(molecular, isLongSummaryType, table, labels)

            val tableContents = generateTableContents(summaryType, summarizer, molecular, labels)

            val filteredContents = tableContents
                .filterNot { (_, value) -> (value.contains(Formats.VALUE_NONE) || value.contains(Formats.VALUE_UNKNOWN)) && !isLongSummaryType }
                .flatMap { (key, value) -> listOf(Cells.createKey(key), Cells.createValue(value)) }
            if (filteredContents.isNotEmpty() || hasTmbData) {
                filteredContents.forEach(table::addCell)
            }

            immunologyGenerator?.addContentsTo(table)

            val (actionableEventsWithUnknownDriver, actionableEventsWithLowOrMediumDriver) =
                summarizer.actionableEventsThatAreNotKeyDrivers().partition { it.driverLikelihood == null }
            val ploidy = molecular.characteristics.ploidy

            if (actionableEventsWithLowOrMediumDriver.isNotEmpty()) {
                table.addCell(Cells.createKey(labels.molecularWgsPotentialEventsNoHighDriver()))
                table.addCell(potentiallyActionableEventsCell(actionableEventsWithLowOrMediumDriver, ploidy))
            }
            if (actionableEventsWithUnknownDriver.isNotEmpty()) {
                table.addCell(Cells.createKey(labels.molecularWgsPotentialEventsNoTumorDriver()))
                table.addCell(potentiallyActionableEventsCell(actionableEventsWithUnknownDriver, ploidy))
            }

            if (filteredContents.isEmpty() && !hasTmbData && actionableEventsWithLowOrMediumDriver.isEmpty()
                && actionableEventsWithUnknownDriver.isEmpty()
            ) {
                table.addCell(Cells.createSpanningContent(labels.molecularWgsNoRelevantAlterations(), table))
            }
        } else {
            table.addCell(Cells.createSpanningContent(labels.molecularWgsInsufficientQuality(), table))
        }
        return table
    }

    fun createTmbCells(
        molecular: MolecularTest,
        isLongSummaryType: Boolean,
        table: Table,
        labels: ReportLabels
    ): Boolean {
        val tmlUnknownAndTmbKnown =
            molecular.characteristics.tumorMutationalLoad == null && molecular.characteristics.tumorMutationalBurden != null
        val tmlAndTmbKnown =
            molecular.characteristics.tumorMutationalLoad != null && molecular.characteristics.tumorMutationalBurden != null
        if (tmlUnknownAndTmbKnown) {
            val tmbStatus = MolecularCharacteristicFormat.formatTumorMutationalBurden(molecular.characteristics, true)
            table.addCell(Cells.createKey(labels.molecularWgsTmbLabel()))
            table.addCell(tumorMutationalLoadAndTumorMutationalBurdenStatusCell(molecular, tmbStatus))
            return true
        } else if (isLongSummaryType || tmlAndTmbKnown) {
            val tmlAndTmbStatus = tumorMutationalLoadAndTumorMutationalBurdenStatus(molecular)
            table.addCell(Cells.createKey(labels.molecularWgsTmlTmbLabel()))
            table.addCell(tumorMutationalLoadAndTumorMutationalBurdenStatusCell(molecular, tmlAndTmbStatus))
            return true
        }
        return false
    }

    fun tumorOriginPredictionCell(molecular: MolecularTest): Cell {
        val originSummary = TumorOriginInterpreter.create(molecular).generateSummaryString()

        val wgsMolecular = MolecularHistory(listOf(molecular)).latestOrangeMolecularRecord()
        val paragraph = Paragraph(Text(originSummary).addStyle(Styles.tableHighlightStyle()))
        if (molecular.characteristics.purity != null && wgsMolecular?.hasSufficientQualityButLowPurity() == true) {
            paragraph.add(Text(" (low purity)").addStyle(Styles.tableNoticeStyle()))
        }
        return Cells.create(paragraph)
    }

    private fun tumorMutationalLoadAndTumorMutationalBurdenStatusCell(molecular: MolecularTest, status: String): Cell {
        val paragraph = Paragraph(Text(status).addStyle(Styles.tableHighlightStyle()))
        val purity = molecular.characteristics.purity
        val wgsMolecular = if (molecular.experimentType == ExperimentType.HARTWIG_WHOLE_GENOME) molecular else null
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
                    } else " (annotated as not a driver)"
                }
            }
            listOf(
                Text(driver.eventDisplay()).addStyle(Styles.tableHighlightStyle()),
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
        summaryType: SummaryType,
        summarizer: MolecularDriversSummarizer,
        molecular: MolecularTest,
        labels: ReportLabels
    ): List<Pair<String, String>> {
        val characteristicsGenerator = MolecularCharacteristicsGenerator(molecular, labels)
        return buildList {
            add(labels.molecularWgsMsStability() to characteristicsGenerator.createMSStabilityString())
            add(labels.molecularWgsHrStatus() to characteristicsGenerator.createHRStatusString())
            add(labels.molecularWgsDriverMutations() to formatList(summarizer.keyVariantEvents()))
            if (summaryType == SummaryType.DETAILS) {
                add(labels.molecularWgsOtherMutations() to formatList(summarizer.otherVariantEvents()))
            }
            add(labels.molecularWgsAmplifiedGenes() to formatList(summarizer.keyAmplifiedGeneEvents()))
            add(labels.molecularWgsDeletedGenes() to formatList(summarizer.keyDeletedGeneEvents()))
            add(labels.molecularWgsHomozygouslyDisruptedGenes() to formatList(summarizer.keyHomozygouslyDisruptedGenes()))
            add(labels.molecularWgsGeneFusions() to formatList(summarizer.keyFusionEvents()))
            add(labels.molecularWgsDriverVirus() to formatList(summarizer.keyVirusEvents()))
        }
    }
}