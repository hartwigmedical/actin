package com.hartwig.actin.report.pdf.chapters

import com.hartwig.actin.datamodel.trial.TrialSource
import com.hartwig.actin.report.datamodel.Report
import com.hartwig.actin.report.interpretation.InterpretedCohort
import com.hartwig.actin.report.interpretation.InterpretedCohortFactory
import com.hartwig.actin.report.pdf.ReportContentProvider
import com.hartwig.actin.report.pdf.chapters.ChapterContentFunctions.addGenerators
import com.hartwig.actin.report.pdf.tables.TableGenerator
import com.hartwig.actin.report.pdf.tables.trial.ActinTrialGeneratorFunctions.partitionByLocation
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

        val evaluableCohorts = InterpretedCohortFactory.createEvaluableCohorts(
            report.treatmentMatch, report.config.filterOnSOCExhaustionAndTumorType
        )
        val source = TrialSource.fromDescription(report.requestingHospital)
        val (primaryEvaluableCohorts, otherEvaluableCohorts) = partitionByLocation(evaluableCohorts, source)

        val nonEvaluableCohorts = InterpretedCohortFactory.createNonEvaluableCohorts(report.treatmentMatch)
        val (primaryNonEvaluableCohorts, otherNonEvaluableCohorts) = partitionByLocation(nonEvaluableCohorts, source)

        val primaryCohortGenerators =
            createActinTrialGenerators(
                primaryEvaluableCohorts,
                primaryNonEvaluableCohorts,
                report.requestingHospital,
                false
            )

        val otherCohortGenerators =
            otherEvaluableCohorts.takeIf { it.isNotEmpty() }
                ?.groupBy { it.source }
                ?.map { (source, cohortsPerSource) ->
                    createActinTrialGenerators(
                        cohortsPerSource,
                        otherNonEvaluableCohorts.filter { it.source == source },
                        source?.description,
                        true
                    )
                }
                ?.flatten()
                ?.filter { (it is EligibleActinTrialsGenerator && it.getCohortSize() > 0) || (it is IneligibleActinTrialsGenerator && it.getCohortSize() > 0) }
                ?: emptyList()

        val (_, eligible) = EligibleActinTrialsGenerator.forOpenCohorts(
            evaluableCohorts.partition { it.ignore }.second, report.requestingHospital, contentWidth(), slotsAvailable = true
        )
        val (localTrialGenerator, nonLocalTrialGenerator) = reportContentProvider.provideExternalTrialsTables(
            report.patientRecord, eligible, contentWidth()
        )
        val allEvidenceSources = report.patientRecord.molecularHistory.molecularTests.map { it.evidenceSource }.toSet()

        return primaryCohortGenerators + otherCohortGenerators +
                listOfNotNull(
                    filteredNationalTrials.takeIf { it.isNotEmpty() }?.let {
                        EligibleExternalTrialsGenerator(
                            allEvidenceSources, it, contentWidth(), it.size, report.config.countryOfReference, false
                        )
                    },
                    filteredInternationalTrials.takeIf { it.isNotEmpty() }?.let {
                        EligibleExternalTrialsGenerator(
                            allEvidenceSources, it, contentWidth(), it.size, isFilteredTrialsTable = false
                        )
                    },
                    localTrialGenerator.takeIf { externalTrialsOnly },
                    nonLocalTrialGenerator.takeIf { externalTrialsOnly }
                )
    }

    private fun createActinTrialGenerators(
        cohorts: List<InterpretedCohort>,
        nonEvaluableCohorts: List<InterpretedCohort>,
        source: String?,
        includeLocation: Boolean
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
        return listOfNotNull(
            eligibleActinTrialsClosedCohortsGenerator.takeIf { !externalTrialsOnly },
            ineligibleActinTrialsGenerator.takeIf { !(includeIneligibleTrialsInSummary || externalTrialsOnly) },
            ineligibleActinTrialsClosedCohortsGenerator.takeIf { !((includeIneligibleTrialsInSummary || externalTrialsOnly) || enableExtendedMode) },
            ineligibleActinTrialsNonEvaluableAndIgnoredCohortsGenerator.takeIf { !(includeIneligibleTrialsInSummary || externalTrialsOnly) })
    }
}