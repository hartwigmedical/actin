package com.hartwig.actin.report.pdf.chapters

import com.hartwig.actin.datamodel.molecular.NO_EVIDENCE_SOURCE
import com.hartwig.actin.datamodel.trial.TrialSource
import com.hartwig.actin.report.datamodel.Report
import com.hartwig.actin.report.interpretation.InterpretedCohortFactory
import com.hartwig.actin.report.pdf.ReportContentProvider
import com.hartwig.actin.report.pdf.chapters.ChapterContentFunctions.addGenerators
import com.hartwig.actin.report.pdf.tables.TableGenerator
import com.hartwig.actin.report.pdf.tables.trial.ActinTrialGeneratorFunctions.partitionBaseOnLocation
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

        val source = TrialSource.fromDescription(report.treatmentMatch.trialSource)
        val (ownIgnoredCohorts, otherIgnoredCohorts) = partitionBaseOnLocation(ignoredCohorts, source)
        val (ownNonIgnoredCohorts, otherNonIgnoredCohorts) = partitionBaseOnLocation(nonIgnoredCohorts, source)

        val nonEvaluableCohorts = InterpretedCohortFactory.createNonEvaluableCohorts(report.treatmentMatch)
        val (ownNonEvaluableCohorts, otherNonEvaluableCohorts) = partitionBaseOnLocation(nonEvaluableCohorts, source)

        val ownEligibleActinTrialsClosedCohortsGenerator = EligibleActinTrialsGenerator.forClosedCohorts(
            ownNonIgnoredCohorts,
            report.treatmentMatch.trialSource,
            contentWidth(),
            includeLocation = false
        )
        val ownIneligibleActinTrialsGenerator = IneligibleActinTrialsGenerator.forOpenCohorts(
            ownNonIgnoredCohorts, report.treatmentMatch.trialSource, contentWidth(), enableExtendedMode
        )
        val ownIneligibleActinTrialsClosedCohortsGenerator = IneligibleActinTrialsGenerator.forClosedCohorts(
            ownNonIgnoredCohorts, report.treatmentMatch.trialSource, contentWidth()
        )
        val ownIneligibleActinTrialsNonEvaluableAndIgnoredCohortsGenerator =
            IneligibleActinTrialsGenerator.forNonEvaluableAndIgnoredCohorts(
                ownIgnoredCohorts, ownNonEvaluableCohorts, report.treatmentMatch.trialSource, contentWidth()
            )

        val (otherEligibleActinTrialsClosedCohortsGenerators, otherIneligibleActinTrialsGenerators, otherIneligibleActinTrialsClosedCohortsGenerators) =
            otherNonIgnoredCohorts.groupBy { it.source }
                .map { (source, cohortsPerSource) ->
                    Triple(
                        EligibleActinTrialsGenerator.forClosedCohorts(
                            cohortsPerSource,
                            source?.description,
                            contentWidth(),
                            true
                        ).takeIf { it.getCohortSize() > 0 },
                        IneligibleActinTrialsGenerator.forOpenCohorts(
                            cohortsPerSource, source?.description, contentWidth(), enableExtendedMode, true
                        ).takeIf { it.getCohortSize() > 0 },
                        IneligibleActinTrialsGenerator.forClosedCohorts(
                            cohortsPerSource, source?.description, contentWidth(), true
                        ).takeIf { it.getCohortSize() > 0 }
                    )
                }.takeIf { it.isNotEmpty() }?.let { generators ->
                    Triple(
                        generators.mapNotNull { it.first },
                        generators.mapNotNull { it.second },
                        generators.mapNotNull { it.third }
                    )
                } ?: Triple(null, null, null)


        val otherIneligibleActinTrialsNonEvaluableAndIgnoredCohortsGenerator =
            otherNonEvaluableCohorts.groupBy { it.source }.map { (source, cohortsPerSource) ->
                IneligibleActinTrialsGenerator.forNonEvaluableAndIgnoredCohorts(
                    otherIgnoredCohorts, cohortsPerSource, source?.description, contentWidth(), true
                )
            }

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
            ownEligibleActinTrialsClosedCohortsGenerator.takeIf { !externalTrialsOnly },
            ownIneligibleActinTrialsGenerator.takeIf { !(includeIneligibleTrialsInSummary || externalTrialsOnly) },
            ownIneligibleActinTrialsClosedCohortsGenerator.takeIf { !((includeIneligibleTrialsInSummary || externalTrialsOnly) || enableExtendedMode) },
            ownIneligibleActinTrialsNonEvaluableAndIgnoredCohortsGenerator.takeIf { !(includeIneligibleTrialsInSummary || externalTrialsOnly) },

            otherEligibleActinTrialsClosedCohortsGenerators.takeIf { !externalTrialsOnly },
            otherIneligibleActinTrialsGenerators.takeIf { !(includeIneligibleTrialsInSummary || externalTrialsOnly) },
            otherIneligibleActinTrialsClosedCohortsGenerators.takeIf { !((includeIneligibleTrialsInSummary || externalTrialsOnly) || enableExtendedMode) },
            otherIneligibleActinTrialsNonEvaluableAndIgnoredCohortsGenerator.takeIf { !(includeIneligibleTrialsInSummary || externalTrialsOnly) },

            filteredNationalTrials.takeIf { it.isNotEmpty() }?.let {
                EligibleExternalTrialsGenerator(
                    allEvidenceSources,
                    it,
                    contentWidth(),
                    it.size,
                    report.config.countryOfReference,
                    false
                )
            },
            filteredInternationalTrials.takeIf { it.isNotEmpty() }?.let {
                EligibleExternalTrialsGenerator(
                    allEvidenceSources,
                    it,
                    contentWidth(),
                    it.size,
                    isFilteredTrialsTable = false
                )
            },

            localTrialGenerator.takeIf { externalTrialsOnly },
            nonLocalTrialGenerator.takeIf { externalTrialsOnly }
        ).flatMap {
            when (it) {
                is List<*> -> it.filterIsInstance<TableGenerator>()
                is TableGenerator -> listOf(it)
                else -> emptyList()
            }
        }
    }
}