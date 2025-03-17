package com.hartwig.actin.report.pdf.chapters

import com.hartwig.actin.datamodel.trial.TrialSource
import com.hartwig.actin.report.datamodel.Report
import com.hartwig.actin.report.interpretation.InterpretedCohort
import com.hartwig.actin.report.pdf.chapters.ChapterContentFunctions.addGenerators
import com.hartwig.actin.report.pdf.tables.TableGenerator
import com.hartwig.actin.report.pdf.tables.trial.*
import com.hartwig.actin.report.pdf.tables.trial.ActinTrialGeneratorFunctions.partitionBySource
import com.hartwig.actin.report.pdf.util.Tables
import com.hartwig.actin.report.trial.TrialsProvider
import com.itextpdf.kernel.geom.PageSize
import com.itextpdf.layout.Document

class TrialMatchingChapter(
    private val report: Report,
    private val enableExtendedMode: Boolean,
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

        val (primaryEvaluableCohorts, otherEvaluableCohorts) = partitionBySource(trialsProvider.allEvaluableCohorts(), requestingSource)
        val (primaryNonEvaluableCohorts, otherNonEvaluableCohorts) = partitionBySource(trialsProvider.allNonEvaluableCohorts(), requestingSource)

        val primaryCohortGenerators = createActinTrialGenerators(
            primaryEvaluableCohorts, primaryNonEvaluableCohorts, report.requestingHospital, false
        )

        val otherCohortGenerators =
            otherEvaluableCohorts.takeIf { it.isNotEmpty() }?.groupBy { it.source }?.map { (source, cohortsPerSource) ->
                source to createActinTrialGenerators(
                    cohortsPerSource, otherNonEvaluableCohorts.filter { it.source == source }, source?.description, true
                )
            }
                ?.flatMap { it.second.map { s -> it.first to s } }
                ?.filter { (_, generator) ->
                     (generator is EligibleActinTrialsGenerator && generator.getCohortSize() > 0) || (generator is IneligibleActinTrialsGenerator && generator.getCohortSize() > 0)
                }
                ?.map { it.second }
                ?: emptyList()

        val (localTrialGeneratorIncluded, nonLocalTrialGeneratorIncluded) = EligibleExternalTrialsGenerator.provideExternalTrialsGenerators(
            trialsProvider, contentWidth(), report.config.countryOfReference, true
        )

        val (localTrialGeneratorExcluded, nonLocalTrialGeneratorExcluded) = EligibleExternalTrialsGenerator.provideExternalTrialsGenerators(
            trialsProvider, contentWidth(), report.config.countryOfReference, false
        )

        return primaryCohortGenerators + otherCohortGenerators + listOfNotNull(localTrialGeneratorExcluded,
            nonLocalTrialGeneratorExcluded,
            localTrialGeneratorIncluded.takeIf { externalTrialsOnly },
            nonLocalTrialGeneratorIncluded.takeIf { externalTrialsOnly })
    }

    private fun createActinTrialGenerators(
        cohorts: List<InterpretedCohort>, nonEvaluableCohorts: List<InterpretedCohort>, source: String?, includeLocation: Boolean
    ): List<TableGenerator> {
        val (ignoredCohorts, nonIgnoredCohorts) = cohorts.partition { it.ignore }

        val eligibleActinTrialsClosedCohortsGenerator = EligibleActinTrialsGenerator.forClosedCohorts(
            nonIgnoredCohorts, source, contentWidth(), includeLocation = includeLocation
        )
        val ineligibleActinTrialsGenerator = IneligibleActinTrialsGenerator.forOpenCohorts(
            nonIgnoredCohorts, source, contentWidth(), enableExtendedMode, includeLocation = includeLocation
        )
        val ineligibleActinTrialsClosedCohortsGenerator = IneligibleActinTrialsGenerator.forClosedCohorts(
            nonIgnoredCohorts, source, contentWidth(), includeLocation = includeLocation
        )
        val ineligibleActinTrialsNonEvaluableAndIgnoredCohortsGenerator = IneligibleActinTrialsGenerator.forNonEvaluableAndIgnoredCohorts(
            ignoredCohorts, nonEvaluableCohorts, source, contentWidth(), includeLocation = includeLocation
        )
        return listOfNotNull(eligibleActinTrialsClosedCohortsGenerator.takeIf { !externalTrialsOnly },
            ineligibleActinTrialsGenerator.takeIf { !(includeIneligibleTrialsInSummary || externalTrialsOnly) },
            ineligibleActinTrialsClosedCohortsGenerator.takeIf { !((includeIneligibleTrialsInSummary || externalTrialsOnly) || enableExtendedMode) },
            ineligibleActinTrialsNonEvaluableAndIgnoredCohortsGenerator.takeIf { !(includeIneligibleTrialsInSummary || externalTrialsOnly) })
    }
}