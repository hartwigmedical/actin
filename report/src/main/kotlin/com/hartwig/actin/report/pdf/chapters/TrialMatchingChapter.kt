package com.hartwig.actin.report.pdf.chapters

import com.hartwig.actin.datamodel.trial.TrialSource
import com.hartwig.actin.report.datamodel.Report
import com.hartwig.actin.report.interpretation.InterpretedCohort
import com.hartwig.actin.report.pdf.chapters.ChapterContentFunctions.addGenerators
import com.hartwig.actin.report.pdf.tables.TableGenerator
import com.hartwig.actin.report.pdf.tables.trial.ActinTrialsGenerator
import com.hartwig.actin.report.pdf.tables.trial.EligibleTrialsGenerator
import com.hartwig.actin.report.pdf.tables.trial.IneligibleActinTrialsGenerator
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
        val requestingSource = TrialSource.fromDescription(report.requestingHospital)
        val homeCountry = report.config.countryOfReference
        val externalTrials = trialsProvider.summarizeExternalTrials()

        val localTrialGenerators = createActinTrialGenerators(
            trialsProvider.evaluableCohorts(), trialsProvider.nonEvaluableCohorts(), requestingSource
        )
        val localExternalTrialGenerator = EligibleTrialsGenerator.forOpenCohorts(
            emptyList(),
            externalTrials.nationalTrials.filtered,
            requestingSource,
            homeCountry,
            contentWidth()
        ).takeIf { externalTrialsOnly }
        val nonLocalTrialGenerator = EligibleTrialsGenerator.forOpenCohorts(
            emptyList(),
            externalTrials.internationalTrials.filtered,
            requestingSource,
            homeCountry,
            contentWidth()
        ).takeIf { externalTrialsOnly }

        return localTrialGenerators + listOfNotNull(localExternalTrialGenerator, nonLocalTrialGenerator)
    }

    private fun createActinTrialGenerators(
        cohorts: List<InterpretedCohort>,
        nonEvaluableCohorts: List<InterpretedCohort>,
        source: TrialSource?
    ): List<ActinTrialsGenerator> {
        val (ignoredCohorts, nonIgnoredCohorts) = cohorts.partition { it.ignore }

        val eligibleActinTrialsClosedCohortsGenerator = EligibleTrialsGenerator.forClosedCohorts(
            nonIgnoredCohorts, source, contentWidth()
        )
        val ineligibleActinTrialsGenerator = IneligibleActinTrialsGenerator.forCohorts(nonIgnoredCohorts, source, contentWidth())
        val ineligibleActinTrialsNonEvaluableAndIgnoredCohortsGenerator = IneligibleActinTrialsGenerator.forNonEvaluableAndIgnoredCohorts(
            ignoredCohorts, nonEvaluableCohorts, source, contentWidth()
        )
        return listOfNotNull(
            eligibleActinTrialsClosedCohortsGenerator.takeIf { !externalTrialsOnly },
            ineligibleActinTrialsGenerator.takeIf { !(includeIneligibleTrialsInSummary || externalTrialsOnly) },
            ineligibleActinTrialsNonEvaluableAndIgnoredCohortsGenerator.takeIf { !(includeIneligibleTrialsInSummary || externalTrialsOnly) })
    }
}