package com.hartwig.actin.report.pdf.chapters

import com.hartwig.actin.datamodel.molecular.NO_EVIDENCE_SOURCE
import com.hartwig.actin.report.datamodel.Report
import com.hartwig.actin.report.interpretation.InterpretedCohortFactory
import com.hartwig.actin.report.pdf.ReportContentProvider
import com.hartwig.actin.report.pdf.chapters.ChapterContentFunctions.addGenerators
import com.hartwig.actin.report.pdf.tables.TableGenerator
import com.hartwig.actin.report.pdf.tables.trial.EligibleActinTrialsGenerator
import com.hartwig.actin.report.pdf.tables.trial.EligibleExternalTrialsGenerator
import com.hartwig.actin.report.pdf.tables.trial.ExternalTrialSummary
import com.hartwig.actin.report.pdf.tables.trial.IneligibleActinTrialsGenerator
import com.hartwig.actin.report.pdf.util.Tables
import com.itextpdf.kernel.geom.PageSize
import com.itextpdf.layout.Document

class TrialMatchingChapter(
    private val report: Report,
    private val enableExtendedMode: Boolean,
    private val includeIneligibleTrialsInSummary: Boolean,
    private val externalTrialsOnly: Boolean,
    private val reportContentProvider: ReportContentProvider,
    private val filteredNationalTrials: Set<ExternalTrialSummary>,
    private val filteredInternationalTrials: Set<ExternalTrialSummary>,
    override val include: Boolean
) : ReportChapter {

    override fun name(): String {
        return "Trial Matching Summary"
    }

    override fun pageSize(): PageSize {
        return PageSize.A4
    }

    override fun render(document: Document) {
        addChapterTitle(document)
        addTrialMatchingOverview(document)
    }

    private fun addTrialMatchingOverview(document: Document) {
        val table = Tables.createSingleColWithWidth(contentWidth())
        addGenerators(createGenerators(), table, false)
        document.add(table)
    }

    fun createGenerators(): List<TableGenerator> {
        val (ignoredCohorts, nonIgnoredCohorts) = InterpretedCohortFactory.createEvaluableCohorts(
            report.treatmentMatch,
            report.config.filterOnSOCExhaustionAndTumorType
        )
            .partition { it.ignore }
        val nonEvaluableCohorts = InterpretedCohortFactory.createNonEvaluableCohorts(report.treatmentMatch)
        val (_, evaluated) =
            EligibleActinTrialsGenerator.forOpenCohorts(
                nonIgnoredCohorts, report.treatmentMatch.trialSource, contentWidth(), slotsAvailable = true
            )

        val (localTrialGenerator, nonLocalTrialGenerator) = reportContentProvider.provideExternalTrialsTables(
            report.patientRecord,
            evaluated,
            contentWidth()
        )

        val allEvidenceSources =
            report.patientRecord.molecularHistory.molecularTests.map { it.evidenceSource }.filter { it != NO_EVIDENCE_SOURCE }.toSet()

        return listOfNotNull(
            EligibleActinTrialsGenerator.forClosedCohorts(
                nonIgnoredCohorts,
                report.treatmentMatch.trialSource,
                contentWidth(),
            ).takeIf { !externalTrialsOnly },
            if (includeIneligibleTrialsInSummary || externalTrialsOnly) null else {
                IneligibleActinTrialsGenerator.forOpenCohorts(
                    nonIgnoredCohorts,
                    report.treatmentMatch.trialSource,
                    contentWidth(),
                    enableExtendedMode
                )
            },
            if ((includeIneligibleTrialsInSummary || externalTrialsOnly) || enableExtendedMode) null else {
                IneligibleActinTrialsGenerator.forClosedCohorts(nonIgnoredCohorts, report.treatmentMatch.trialSource, contentWidth())
            },
            if (includeIneligibleTrialsInSummary || externalTrialsOnly) null else {
                IneligibleActinTrialsGenerator.forNonEvaluableAndIgnoredCohorts(
                    ignoredCohorts, nonEvaluableCohorts, report.treatmentMatch.trialSource, contentWidth()
                )
            },
            if (filteredNationalTrials.isEmpty()) null else {
                EligibleExternalTrialsGenerator(
                    allEvidenceSources,
                    filteredNationalTrials,
                    contentWidth(),
                    0,
                    report.config.countryOfReference,
                    true
                )
            },
            if (filteredInternationalTrials.isEmpty()) null else {
                EligibleExternalTrialsGenerator(
                    allEvidenceSources,
                    filteredInternationalTrials,
                    contentWidth(),
                    0,
                    isFilteredTrialsTable = true
                )
            },
            localTrialGenerator.takeIf {
                externalTrialsOnly
            },
            nonLocalTrialGenerator.takeIf {
                externalTrialsOnly
            }
        )
    }
}