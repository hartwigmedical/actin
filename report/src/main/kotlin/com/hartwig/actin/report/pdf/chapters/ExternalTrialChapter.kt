package com.hartwig.actin.report.pdf.chapters

import com.hartwig.actin.PatientRecord
import com.hartwig.actin.molecular.datamodel.NO_EVIDENCE_SOURCE
import com.hartwig.actin.molecular.interpretation.AggregatedEvidenceFactory
import com.hartwig.actin.report.datamodel.Report
import com.hartwig.actin.report.interpretation.EvaluatedCohort
import com.hartwig.actin.report.interpretation.EvaluatedCohortFactory
import com.hartwig.actin.report.pdf.tables.TableGenerator
import com.hartwig.actin.report.pdf.tables.trial.EligibleActinTrialsGenerator
import com.hartwig.actin.report.pdf.tables.trial.EligibleLocalExternalTrialsGenerator
import com.hartwig.actin.report.pdf.tables.trial.EligibleOtherCountriesExternalTrialsGenerator
import com.hartwig.actin.report.pdf.tables.trial.ExternalTrialSummarizer
import com.hartwig.actin.report.pdf.util.Cells
import com.hartwig.actin.report.pdf.util.Tables
import com.itextpdf.kernel.geom.PageSize
import com.itextpdf.layout.Document

class ExternalTrialChapter(private val report: Report, override val include: Boolean) : ReportChapter {

    override fun name(): String {
        return "Trial Eligibility"
    }

    override fun pageSize(): PageSize {
        return PageSize.A4
    }

    override fun render(document: Document) {
        addChapterTitle(document)
        addLongitudinalMolecularHistoryTable(document)
    }

    private fun addLongitudinalMolecularHistoryTable(document: Document) {
        val table = Tables.createSingleColWithWidth(contentWidth())

        val contentWidth = contentWidth()
        val cohorts = EvaluatedCohortFactory.create(report.treatmentMatch, report.config.filterOnSOCExhaustionAndTumorType)
        val (openCohortsWithSlotsGenerator, evaluated) =
            EligibleActinTrialsGenerator.forOpenCohorts(cohorts, report.treatmentMatch.trialSource, contentWidth, slotsAvailable = true)
        val (openCohortsWithoutSlotsGenerator, _) =
            EligibleActinTrialsGenerator.forOpenCohorts(cohorts, report.treatmentMatch.trialSource, contentWidth, slotsAvailable = false)

        val (localTrialGenerator, nonLocalTrialGenerator) = externalTrials(report.patientRecord, evaluated, contentWidth)

        val generators = listOfNotNull(localTrialGenerator, nonLocalTrialGenerator)

        generators.forEachIndexed { i, generator ->
            table.addCell(Cells.createSubTitle(generator.title()))
            table.addCell(Cells.create(generator.contents()))
            if (i < generators.size - 1) {
                table.addCell(Cells.createEmpty())
            }
        }
        document.add(table)
    }

    // TODO (KD): Clean up duplication
    private fun externalTrials(
        patientRecord: PatientRecord, evaluated: List<EvaluatedCohort>, contentWidth: Float
    ): Pair<TableGenerator?, TableGenerator?> {
        val externalEligibleTrials =
            AggregatedEvidenceFactory.mergeMapsOfSets(patientRecord.molecularHistory.molecularTests.map {
                AggregatedEvidenceFactory.create(it).externalEligibleTrialsPerEvent
            })

        val externalTrialSummarizer = ExternalTrialSummarizer(report.config.countryOfReference)
        val externalTrialSummary = externalTrialSummarizer.summarize(
            externalEligibleTrials,
            report.treatmentMatch.trialMatches,
            evaluated
        )
        val allEvidenceSources =
            patientRecord.molecularHistory.molecularTests.map { it.evidenceSource }.filter { it != NO_EVIDENCE_SOURCE }.toSet()
        return Pair(
            if (externalTrialSummary.localTrials.isNotEmpty()) {
                EligibleLocalExternalTrialsGenerator(
                    allEvidenceSources,
                    externalTrialSummary.localTrials,
                    contentWidth,
                    externalTrialSummary.localTrialsFiltered,
                    report.config.countryOfReference
                )
            } else null,
            if (externalTrialSummary.nonLocalTrials.isNotEmpty()) {
                EligibleOtherCountriesExternalTrialsGenerator(
                    allEvidenceSources,
                    externalTrialSummary.nonLocalTrials,
                    contentWidth,
                    externalTrialSummary.nonLocalTrialsFiltered
                )
            } else null
        )
    }
}