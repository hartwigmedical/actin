package com.hartwig.actin.report.pdf.tables.molecular

import com.hartwig.actin.datamodel.algo.TrialMatch
import com.hartwig.actin.datamodel.molecular.DriverLikelihood
import com.hartwig.actin.datamodel.molecular.MolecularRecord
import com.hartwig.actin.datamodel.molecular.evidence.CountryName
import com.hartwig.actin.datamodel.molecular.evidence.ExternalTrial
import com.hartwig.actin.molecular.interpretation.AggregatedEvidenceFactory
import com.hartwig.actin.report.interpretation.ClonalityInterpreter
import com.hartwig.actin.report.interpretation.EvaluatedCohort
import com.hartwig.actin.report.interpretation.EvaluatedCohortsInterpreter
import com.hartwig.actin.report.interpretation.MolecularDriverEntry
import com.hartwig.actin.report.interpretation.MolecularDriverEntryFactory
import com.hartwig.actin.report.interpretation.MolecularDriversInterpreter
import com.hartwig.actin.report.pdf.tables.TableGenerator
import com.hartwig.actin.report.pdf.tables.trial.ExternalTrialSummarizer
import com.hartwig.actin.report.pdf.util.Cells
import com.hartwig.actin.report.pdf.util.Formats
import com.hartwig.actin.report.pdf.util.Tables
import com.hartwig.actin.report.pdf.util.Tables.makeWrapping
import com.itextpdf.layout.element.Table

class MolecularDriversGenerator(
    private val trialSource: String?,
    private val molecular: MolecularRecord,
    private val cohorts: List<EvaluatedCohort>,
    private val trialMatches: List<TrialMatch>,
    private val width: Float,
    private val homeCountry: CountryName
) : TableGenerator {

    override fun title(): String {
        return "Drivers"
    }

    override fun contents(): Table {
        val colWidth = width / 8
        val table = Tables.createFixedWidthCols(colWidth, colWidth * 2, colWidth, colWidth, colWidth, colWidth, colWidth)

        table.addHeaderCell(Cells.createHeader("Type"))
        table.addHeaderCell(Cells.createHeader("Driver"))
        table.addHeaderCell(Cells.createHeader("Driver likelihood"))
        table.addHeaderCell(Cells.createHeader(trialSource?.let { "Trials in $it" } ?: "Trials"))
        table.addHeaderCell(Cells.createHeader("Trials in ${molecular.externalTrialSource}"))
        table.addHeaderCell(Cells.createHeader("Best evidence in ${molecular.evidenceSource}"))
        table.addHeaderCell(Cells.createHeader("Resistance in ${molecular.evidenceSource}"))

        val molecularDriversInterpreter = MolecularDriversInterpreter(molecular.drivers, EvaluatedCohortsInterpreter.fromEvaluatedCohorts(cohorts))

        val externalTrialSummarizer = ExternalTrialSummarizer(homeCountry)
        val externalTrialSummary = externalTrialSummarizer.summarize(AggregatedEvidenceFactory.create(molecular).externalEligibleTrialsPerEvent, trialMatches, cohorts)

        val externalTrialsPerEvents = mergeMapsOfSets(listOf(externalTrialSummary.localTrials, externalTrialSummary.nonLocalTrials))
        val externalTrialsPerSingleEvent = DriverTableFunctions.groupByEvent(externalTrialsPerEvents)

        val factory = MolecularDriverEntryFactory(molecularDriversInterpreter)
        factory.create().forEach { entry: MolecularDriverEntry ->
            table.addCell(Cells.createContent(entry.driverType))
            table.addCell(Cells.createContent(entry.display()))
            table.addCell(Cells.createContent(formatDriverLikelihood(entry.driverLikelihood)))
            table.addCell(Cells.createContent(concat(entry.actinTrials)))
            table.addCell(Cells.createContent(externalTrialsPerSingleEvent[entry.eventName]?.let { concatEligibleTrials(it) } ?: ""))
            table.addCell(Cells.createContent(entry.bestResponsiveEvidence ?: ""))
            table.addCell(Cells.createContent(entry.bestResistanceEvidence ?: ""))
        }
        if (molecularDriversInterpreter.hasPotentiallySubClonalVariants()) {
            val note = "* Variant has > " + Formats.percentage(ClonalityInterpreter.CLONAL_CUTOFF) + " likelihood of being sub-clonal"
            table.addCell(Cells.createSpanningSubNote(note, table))
        }
        return makeWrapping(table)
    }

    companion object {
        private fun formatDriverLikelihood(driverLikelihood: DriverLikelihood?): String {
            return driverLikelihood?.let(DriverLikelihood::toString) ?: Formats.VALUE_UNKNOWN
        }

        private fun concat(treatments: Set<String>): String {
            return treatments.joinToString(", ")
        }

        private fun concatEligibleTrials(externalTrials: Iterable<ExternalTrial>): String {
            return externalTrials.map { it.nctId }.toSet().joinToString(", ")
        }

        private fun mergeMapsOfSets(mapsOfSets: List<Map<String, Iterable<ExternalTrial>>>): Map<String, Set<ExternalTrial>> {
            return mapsOfSets
                .flatMap { it.entries }
                .groupBy({ it.key }, { it.value })
                .mapValues { it.value.flatten().toSet() }
        }
    }
}