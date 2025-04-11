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
        TableGeneratorFunctions.addGenerators(createGenerators(), table, overrideTitleFormatToSubtitle = false)
        document.add(table)
    }

    fun createGenerators(): List<TableGenerator> {
        val requestingSource = TrialSource.fromDescription(report.requestingHospital)
        val externalTrials = trialsProvider.summarizeExternalTrials()

        val localTrialGenerators = createTrialTableGenerators(
            trialsProvider.evaluableCohorts(), trialsProvider.nonEvaluableCohorts(), requestingSource
        )
        val localExternalTrialGenerator = EligibleTrialGenerator.forOpenCohorts(
            cohorts = emptyList(),
            externalTrials = externalTrials.nationalTrials.filtered,
            filteredCount = externalTrials.excludedNationalTrials().size,
            requestingSource = requestingSource,
            countryOfReference = report.config.countryOfReference
        ).takeIf { externalTrialsOnly }

        val nonLocalTrialGenerator = EligibleTrialGenerator.forOpenCohorts(
            cohorts = emptyList(),
            externalTrials = externalTrials.internationalTrials.filtered,
            filteredCount = externalTrials.excludedInternationalTrials().size,
            requestingSource = requestingSource,
            countryOfReference = null,
            forLocalTrials = false
        ).takeIf { externalTrialsOnly }

        val filteredTrialGenerator = EligibleTrialGenerator.forFilteredTrials(
            trials = externalTrials.excludedNationalTrials() + externalTrials.excludedInternationalTrials(),
            countryOfReference = report.config.countryOfReference
        )

        return localTrialGenerators + listOfNotNull(localExternalTrialGenerator, nonLocalTrialGenerator) + filteredTrialGenerator
    }

    private fun createTrialTableGenerators(
        cohorts: List<InterpretedCohort>,
        nonEvaluableCohorts: List<InterpretedCohort>,
        source: TrialSource?
    ): List<TrialTableGenerator> {
        val (ignoredCohorts, nonIgnoredCohorts) = cohorts.partition { it.ignore }

        val eligibleActinTrialsClosedCohortsGenerator = EligibleTrialGenerator.forClosedCohorts(nonIgnoredCohorts, source)
        val ineligibleActinTrialsGenerator = IneligibleTrialGenerator.forEvaluableCohorts(nonIgnoredCohorts, source)
        val nonEvaluableAndIgnoredCohortsGenerator = IneligibleTrialGenerator.forNonEvaluableAndIgnoredCohorts(
            ignoredCohorts, nonEvaluableCohorts, source
        )

        return listOfNotNull(
            eligibleActinTrialsClosedCohortsGenerator.takeIf { !externalTrialsOnly },
            ineligibleActinTrialsGenerator.takeIf { !(includeIneligibleTrialsInSummary || externalTrialsOnly) },
            nonEvaluableAndIgnoredCohortsGenerator.takeIf { !(includeIneligibleTrialsInSummary || externalTrialsOnly) })
    }
}