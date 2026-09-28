package com.hartwig.actin.report.pdf.chapters

import com.hartwig.actin.configuration.ReportConfiguration
import com.hartwig.actin.configuration.TrialMatchingChapterType
import com.hartwig.actin.datamodel.trial.TrialSource
import com.hartwig.actin.report.interpretation.InterpretedCohort
import com.hartwig.actin.report.pdf.ReportLabels
import com.hartwig.actin.report.pdf.tables.TableGenerator
import com.hartwig.actin.report.pdf.tables.TableGeneratorFunctions
import com.hartwig.actin.report.pdf.tables.trial.EligibleTrialGenerator
import com.hartwig.actin.report.pdf.tables.trial.IneligibleTrialGenerator
import com.hartwig.actin.report.pdf.tables.trial.TrialTableGenerator
import com.hartwig.actin.report.pdf.util.Tables
import com.hartwig.actin.report.trial.TrialsProvider
import com.itextpdf.kernel.geom.PageSize
import com.itextpdf.layout.Document

class TrialMatchingDetailsChapter(
    private val configuration: ReportConfiguration,
    private val trialsProvider: TrialsProvider,
    private val labels: ReportLabels
) : ReportChapter {

    override fun name(): String {
        return labels.trialMatching.title()
    }

    override fun pageSize(): PageSize {
        return PageSize.A4.rotate()
    }

    override fun include(): Boolean {
        return configuration.trialMatchingChapterType != TrialMatchingChapterType.NONE
    }

    override fun render(document: Document) {
        addChapterTitle(document)
        addTrialMatchingResults(document)
    }

    private fun addTrialMatchingResults(document: Document) {
        val table = Tables.createSingleColWithWidth(contentWidth())
        TableGeneratorFunctions.addGenerators(createTrialTableGenerators(), table, overrideTitleFormatToSubtitle = false)
        document.add(table)
    }

    fun createTrialTableGenerators(): List<TableGenerator> {
        val requestingSource = TrialSource.fromDescription(configuration.hospitalOfReference)

        val localTrialGenerators = if (configuration.trialMatchingChapterType == TrialMatchingChapterType.STANDARD_ALL_TRIALS) {
            createLocalTrialTableGenerators(
                trialsProvider.evaluableCohorts(),
                trialsProvider.nonEvaluableCohorts(),
                requestingSource
            )
        } else {
            emptyList()
        }

        val includeSpecificExternalGenerators =
            configuration.trialMatchingChapterType == TrialMatchingChapterType.STANDARD_EXTERNAL_TRIALS_ONLY

        val externalTrials = trialsProvider.externalTrials()

        val nationalExternalTrialGenerator = includeSpecificExternalGenerators.takeIf { it }?.let {
            EligibleTrialGenerator.externalOpenAndEligibleCohorts(
                externalTrials,
                requestingSource,
                true,
                trialsProvider.effectiveDutchExternalTrialExclusion,
                labels
            )
        }

        val internationalExternalTrialGenerator = includeSpecificExternalGenerators.takeIf { it }?.let {
            EligibleTrialGenerator.externalOpenAndEligibleCohorts(
                externalTrials,
                requestingSource,
                false,
                trialsProvider.effectiveDutchExternalTrialExclusion,
                labels
            )
        }

        val filteredExternalTrialGenerator =
            EligibleTrialGenerator.filteredExternalTrials(externalTrials, configuration.countryOfReference, labels)

        return listOfNotNull(
            nationalExternalTrialGenerator,
            internationalExternalTrialGenerator,
            filteredExternalTrialGenerator
        ) + localTrialGenerators
    }

    private fun createLocalTrialTableGenerators(
        evaluableCohorts: List<InterpretedCohort>,
        nonEvaluableCohorts: List<InterpretedCohort>,
        source: TrialSource?
    ): List<TrialTableGenerator> {
        val eligibleTrialsClosedCohortsGenerator =
            EligibleTrialGenerator.closedCohorts(evaluableCohorts, source, labels)
        val ineligibleTrialsGenerator = IneligibleTrialGenerator.evaluableCohorts(evaluableCohorts, source, labels)
        val nonEvaluableCohortsGenerator =
            IneligibleTrialGenerator.nonEvaluableCohorts(nonEvaluableCohorts, source, labels)

        return listOf(eligibleTrialsClosedCohortsGenerator, ineligibleTrialsGenerator, nonEvaluableCohortsGenerator)
    }
}