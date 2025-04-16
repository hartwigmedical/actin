package com.hartwig.actin.report.pdf.chapters

import com.hartwig.actin.datamodel.trial.TrialSource
import com.hartwig.actin.report.datamodel.Report
import com.hartwig.actin.report.interpretation.InterpretedCohort
import com.hartwig.actin.report.pdf.chapters.ChapterContentFunctions.addGenerators
import com.hartwig.actin.report.pdf.tables.TableGenerator
import com.hartwig.actin.report.pdf.tables.trial.EligibleTrialGenerator
import com.hartwig.actin.report.pdf.tables.trial.IneligibleTrialGenerator
import com.hartwig.actin.report.pdf.tables.trial.TrialTableGenerator
import com.hartwig.actin.report.pdf.util.Tables
import com.hartwig.actin.report.trial.ExternalTrialSummarizer
import com.hartwig.actin.report.trial.TrialsProvider
import com.itextpdf.kernel.geom.PageSize
import com.itextpdf.layout.Document

class OtherTrialMatchingResultsChapter(
    private val report: Report,
    private val includeIneligibleTrialsInSummary: Boolean,
    private val externalTrialsOnly: Boolean,
    private val trialsProvider: TrialsProvider,
    override val include: Boolean
) : ReportChapter {

    override fun name(): String {
        return "Other Trial Matching Results"
    }

    override fun pageSize(): PageSize {
        return PageSize.A4
    }

    override fun render(document: Document) {
        addChapterTitle(document)
        addOtherTrialMatchingResults(document)
    }

    private fun addOtherTrialMatchingResults(document: Document) {
        val table = Tables.createSingleColWithWidth(contentWidth())
        addGenerators(createGenerators(), table, false)
        document.add(table)
    }

    fun createGenerators(): List<TableGenerator> {
        val requestingSource = TrialSource.fromDescription(report.requestingHospital)
        val externalTrials = trialsProvider.externalTrials()

        val localTrialGenerators = createTrialTableGenerators(
            trialsProvider.evaluableCohorts(), trialsProvider.nonEvaluableCohorts(), requestingSource
        )
        val localExternalTrialGenerator = EligibleTrialGenerator.forOpenCohorts(
            emptyList(),
            ExternalTrialSummarizer.summarize(externalTrials.nationalTrials.filtered),
            externalTrials.excludedNationalTrials().groupBy { ewt -> ewt.trial.nctId }.size,
            requestingSource,
            report.config.countryOfReference,
            contentWidth()
        ).takeIf { externalTrialsOnly }
        val nonLocalTrialGenerator = EligibleTrialGenerator.forOpenCohorts(
            emptyList(),
            ExternalTrialSummarizer.summarize(externalTrials.internationalTrials.filtered),
            externalTrials.excludedInternationalTrials().groupBy { ewt -> ewt.trial.nctId }.size,
            requestingSource,
            null,
            contentWidth(),
            false
        ).takeIf { externalTrialsOnly }
        val filteredTrialGenerator = EligibleTrialGenerator.forFilteredTrials(
            ExternalTrialSummarizer.summarize(externalTrials.excludedNationalTrials() + externalTrials.excludedInternationalTrials()),
            report.config.countryOfReference,
            contentWidth()
        )

        return localTrialGenerators + listOfNotNull(localExternalTrialGenerator, nonLocalTrialGenerator) + filteredTrialGenerator
    }

    private fun createTrialTableGenerators(
        cohorts: List<InterpretedCohort>,
        nonEvaluableCohorts: List<InterpretedCohort>,
        source: TrialSource?
    ): List<TrialTableGenerator> {
        val (ignoredCohorts, nonIgnoredCohorts) = cohorts.partition { it.ignore }

        val eligibleActinTrialsClosedCohortsGenerator = EligibleTrialGenerator.forClosedCohorts(
            nonIgnoredCohorts, source, contentWidth()
        )
        val ineligibleActinTrialsGenerator = IneligibleTrialGenerator.forEvaluableCohorts(nonIgnoredCohorts, source, contentWidth())
        val nonEvaluableAndIgnoredCohortsGenerator = IneligibleTrialGenerator.forNonEvaluableAndIgnoredCohorts(
            ignoredCohorts, nonEvaluableCohorts, source, contentWidth()
        )

        return listOfNotNull(
            eligibleActinTrialsClosedCohortsGenerator.takeIf { !externalTrialsOnly },
            ineligibleActinTrialsGenerator.takeIf { !(includeIneligibleTrialsInSummary || externalTrialsOnly) },
            nonEvaluableAndIgnoredCohortsGenerator.takeIf { !(includeIneligibleTrialsInSummary || externalTrialsOnly) })
    }
}