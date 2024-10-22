package com.hartwig.actin.report.pdf.chapters

import com.hartwig.actin.report.datamodel.Report
import com.hartwig.actin.report.interpretation.CohortFactory
import com.hartwig.actin.report.pdf.ReportContentProvider
import com.hartwig.actin.report.pdf.tables.trial.EligibleActinTrialsGenerator
import com.hartwig.actin.report.pdf.tables.trial.IneligibleActinTrialsGenerator
import com.hartwig.actin.report.pdf.util.Cells
import com.hartwig.actin.report.pdf.util.Tables
import com.itextpdf.kernel.geom.PageSize
import com.itextpdf.layout.Document

class TrialMatchingChapter(
    private val report: Report,
    private val enableExtendedMode: Boolean,
    private val includeIneligibleTrialsInSummary: Boolean,
    private val externalTrialsOnly: Boolean,
    private val reportContentProvider: ReportContentProvider,
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
        val cohorts = CohortFactory.create(report.treatmentMatch, report.config.filterOnSOCExhaustionAndTumorType)
        val nonEvaluableAndIgnoredCohorts = CohortFactory.createNonEvaluableAndIgnoredCohorts(
            report.treatmentMatch,
            report.config.filterOnSOCExhaustionAndTumorType
        )
        val (_, evaluated) =
            EligibleActinTrialsGenerator.forOpenCohorts(cohorts, report.treatmentMatch.trialSource, contentWidth(), slotsAvailable = true)

        val (localTrialGenerator, nonLocalTrialGenerator) = reportContentProvider.provideExternalTrialsTables(
            report.patientRecord,
            evaluated,
            contentWidth()
        )
        val generators = listOfNotNull(
            EligibleActinTrialsGenerator.forClosedCohorts(
                cohorts,
                report.treatmentMatch.trialSource,
                contentWidth(),
            ).takeIf { !externalTrialsOnly },
            if (includeIneligibleTrialsInSummary || externalTrialsOnly) null else {
                IneligibleActinTrialsGenerator.forOpenCohorts(
                    cohorts, report.treatmentMatch.trialSource, contentWidth(), enableExtendedMode
                )
            },
            if ((includeIneligibleTrialsInSummary || externalTrialsOnly) || enableExtendedMode) null else {
                IneligibleActinTrialsGenerator.forClosedCohorts(
                    cohorts, report.treatmentMatch.trialSource, contentWidth()
                )
            },
            if (includeIneligibleTrialsInSummary || externalTrialsOnly) null else {
                IneligibleActinTrialsGenerator.forNonEvaluableCohorts(
                    nonEvaluableAndIgnoredCohorts, report.treatmentMatch.trialSource, contentWidth()
                )
            },
            localTrialGenerator.takeIf {
                externalTrialsOnly
            },
            nonLocalTrialGenerator.takeIf {
                externalTrialsOnly
            }
        )

        for (i in generators.indices) {
            val generator = generators[i]
            table.addCell(Cells.createTitle(generator.title()))
            table.addCell(Cells.create(generator.contents()))
            if (i < generators.size - 1) {
                table.addCell(Cells.createEmpty())
            }
        }
        document.add(table)
    }
}