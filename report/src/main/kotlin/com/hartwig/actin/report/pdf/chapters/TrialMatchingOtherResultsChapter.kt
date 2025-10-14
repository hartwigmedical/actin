package com.hartwig.actin.report.pdf.chapters

import com.hartwig.actin.datamodel.trial.TrialSource
import com.hartwig.actin.report.datamodel.Report
import com.hartwig.actin.report.interpretation.InterpretedCohort
import com.hartwig.actin.report.pdf.tables.TableGenerator
import com.hartwig.actin.report.pdf.tables.TableGeneratorFunctions
import com.hartwig.actin.report.pdf.tables.trial.EligibleTrialGenerator
import com.hartwig.actin.report.pdf.tables.trial.IneligibleTrialGenerator
import com.hartwig.actin.report.pdf.tables.trial.TrialTableGenerator
import com.hartwig.actin.report.pdf.util.Tables
import com.hartwig.actin.report.trial.TrialsProvider
import com.itextpdf.kernel.geom.PageSize
import com.itextpdf.layout.Document

class TrialMatchingOtherResultsChapter(
    private val report: Report,
    private val externalTrialsOnly: Boolean,
    private val trialsProvider: TrialsProvider,
    override val include: Boolean
) : ReportChapter {

    override fun name(): String {
        return "Other Trial Matching Results"
    }

    override fun pageSize(): PageSize {
        return PageSize.A4.rotate()
    }

    override fun render(document: Document) {
        addChapterTitle(document)
        addOtherTrialMatchingResults(document)
    }

    private fun addOtherTrialMatchingResults(document: Document) {
        val table = Tables.createSingleColWithWidth(contentWidth())
        TableGeneratorFunctions.addGenerators(createTrialTableGenerators(), table, overrideTitleFormatToSubtitle = false)
        document.add(table)
    }

    fun createTrialTableGenerators(): List<TableGenerator> {
        val requestingSource = TrialSource.fromDescription(report.configuration.hospitalOfReference)
        val externalTrials = trialsProvider.externalTrials()

        val localTrialGenerators = createTrialTableGenerators(
            trialsProvider.evaluableCohorts(), trialsProvider.nonEvaluableCohorts(), requestingSource
        )
        val localExternalTrialGenerator = EligibleTrialGenerator.localOpenCohorts(
            emptyList(),
            externalTrials,
            requestingSource,
            report.configuration.countryOfReference
        ).takeIf { externalTrialsOnly }

        val nonLocalTrialGenerator = EligibleTrialGenerator.nonLocalOpenCohorts(
            externalTrials,
            requestingSource,
        ).takeIf { externalTrialsOnly }

        val filteredTrialGenerator = EligibleTrialGenerator.forFilteredTrials(
            externalTrials,
            report.configuration.countryOfReference
        )

        return listOfNotNull(localExternalTrialGenerator, nonLocalTrialGenerator, filteredTrialGenerator) + localTrialGenerators
    }

    private fun createTrialTableGenerators(
        cohorts: List<InterpretedCohort>,
        nonEvaluableCohorts: List<InterpretedCohort>,
        source: TrialSource?
    ): List<TrialTableGenerator> {
        val (ignoredCohorts, nonIgnoredCohorts) = cohorts.partition { it.ignore }

        val eligibleTrialsClosedCohortsGenerator = EligibleTrialGenerator.forClosedCohorts(nonIgnoredCohorts, source)
        val ineligibleTrialsGenerator = IneligibleTrialGenerator.forEvaluableCohorts(nonIgnoredCohorts, source)
        val nonEvaluableAndIgnoredCohortsGenerator = IneligibleTrialGenerator.forNonEvaluableAndIgnoredCohorts(
            ignoredCohorts, nonEvaluableCohorts, source
        )

        return listOf(
            eligibleTrialsClosedCohortsGenerator,
            ineligibleTrialsGenerator,
            nonEvaluableAndIgnoredCohortsGenerator
        ).takeIf { !externalTrialsOnly } ?: emptyList()
    }
}