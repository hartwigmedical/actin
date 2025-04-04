package com.hartwig.actin.report.pdf.chapters

import com.hartwig.actin.datamodel.trial.TrialSource
import com.hartwig.actin.report.datamodel.Report
import com.hartwig.actin.report.interpretation.InterpretedCohort
import com.hartwig.actin.report.pdf.chapters.ChapterContentFunctions.addGenerators
import com.hartwig.actin.report.pdf.tables.TableGenerator
import com.hartwig.actin.report.pdf.tables.trial.EligibleTrialTableGenerator
import com.hartwig.actin.report.pdf.tables.trial.FilteredTrialTableGenerator
import com.hartwig.actin.report.pdf.tables.trial.IneligibleTrialTableGenerator
import com.hartwig.actin.report.pdf.tables.trial.TrialTableGenerator
import com.hartwig.actin.report.pdf.util.Tables
import com.hartwig.actin.report.trial.TrialsProvider
import com.itextpdf.kernel.geom.PageSize
import com.itextpdf.layout.Document

class TrialMatchingChapter(
    private val report: Report,
    private val includeIneligibleTrialsInSummary: Boolean,
    private val externalTrialsOnly: Boolean,
    private val trialsProvider: TrialsProvider,
    override val include: Boolean
) : ReportChapter {

    override fun name(): String {
        return "Trial Matching Overview"
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
        addGenerators(getGenerators(), table, false)
        document.add(table)
    }

    fun getGenerators(): List<TableGenerator> {
        val requestingSource = TrialSource.fromDescription(report.requestingHospital)
        val homeCountry = report.config.countryOfReference
        val externalTrials = trialsProvider.summarizeExternalTrials()

        val localTrialGenerators = createTrialTableGenerators(
            trialsProvider.evaluableCohorts(), trialsProvider.nonEvaluableCohorts(), requestingSource
        )
        val localExternalTrialGenerator = EligibleTrialTableGenerator.forOpenCohorts(
            emptyList(),
            externalTrials.nationalTrials.filtered,
            externalTrials.excludedNationalTrials().size,
            requestingSource,
            homeCountry,
            contentWidth()
        ).takeIf { externalTrialsOnly }
        val nonLocalTrialGenerator = EligibleTrialTableGenerator.forOpenCohorts(
            emptyList(),
            externalTrials.internationalTrials.filtered,
            externalTrials.excludedInternationalTrials().size,
            requestingSource,
            null,
            contentWidth(),
            false
        ).takeIf { externalTrialsOnly }
        val filteredTrials = externalTrials.excludedNationalTrials() + externalTrials.excludedInternationalTrials()
        val filteredTrialGenerator = FilteredTrialTableGenerator(filteredTrials, homeCountry, contentWidth())

        return localTrialGenerators + listOfNotNull(localExternalTrialGenerator, nonLocalTrialGenerator) + filteredTrialGenerator
    }

    private fun createTrialTableGenerators(
        cohorts: List<InterpretedCohort>,
        nonEvaluableCohorts: List<InterpretedCohort>,
        source: TrialSource?
    ): List<TrialTableGenerator> {
        val (ignoredCohorts, nonIgnoredCohorts) = cohorts.partition { it.ignore }

        val eligibleActinTrialsClosedCohortsGenerator = EligibleTrialTableGenerator.forClosedCohorts(
            nonIgnoredCohorts, source, contentWidth()
        )
        val ineligibleActinTrialsGenerator = IneligibleTrialTableGenerator.forCohorts(nonIgnoredCohorts, source, contentWidth())
        val nonEvaluableAndIgnoredCohortsGenerator = IneligibleTrialTableGenerator.forNonEvaluableAndIgnoredCohorts(
            ignoredCohorts, nonEvaluableCohorts, source, contentWidth()
        )

        return listOfNotNull(
            eligibleActinTrialsClosedCohortsGenerator.takeIf { !externalTrialsOnly },
            ineligibleActinTrialsGenerator.takeIf { !(includeIneligibleTrialsInSummary || externalTrialsOnly) },
            nonEvaluableAndIgnoredCohortsGenerator.takeIf { !(includeIneligibleTrialsInSummary || externalTrialsOnly) })
    }
}