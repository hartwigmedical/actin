package com.hartwig.actin.report.pdf.chapters

import com.hartwig.actin.configuration.ReportConfiguration
import com.hartwig.actin.configuration.TrialMatchingChapterType
import com.hartwig.actin.datamodel.algo.Evaluation
import com.hartwig.actin.datamodel.algo.EvaluationResult
import com.hartwig.actin.datamodel.algo.TrialMatch
import com.hartwig.actin.datamodel.trial.CohortMetadata
import com.hartwig.actin.datamodel.trial.Eligibility
import com.hartwig.actin.datamodel.trial.TrialIdentification
import com.hartwig.actin.datamodel.trial.TrialSource
import com.hartwig.actin.report.datamodel.Report
import com.hartwig.actin.report.interpretation.EvaluationInterpreter
import com.hartwig.actin.report.interpretation.InterpretedCohort
import com.hartwig.actin.report.pdf.tables.TableGenerator
import com.hartwig.actin.report.pdf.tables.TableGeneratorFunctions
import com.hartwig.actin.report.pdf.tables.trial.EligibleTrialGenerator
import com.hartwig.actin.report.pdf.tables.trial.IneligibleTrialGenerator
import com.hartwig.actin.report.pdf.tables.trial.TrialTableGenerator
import com.hartwig.actin.report.pdf.tables.trial.TrialType
import com.hartwig.actin.report.pdf.util.Cells
import com.hartwig.actin.report.pdf.util.Formats
import com.hartwig.actin.report.pdf.util.Styles
import com.hartwig.actin.report.pdf.util.Tables
import com.hartwig.actin.report.trial.TrialsProvider
import com.itextpdf.kernel.geom.PageSize
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.AreaBreak
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Table
import com.itextpdf.layout.properties.AreaBreakType

private const val INDENT_WIDTH = 10f
private const val KEY_WIDTH = 90f

class TrialMatchingDetailsChapter(
    private val report: Report,
    private val configuration: ReportConfiguration,
    private val trialsProvider: TrialsProvider
) : ReportChapter {

    override fun name(): String {
        return "Trial Matching Details"
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
        if (configuration.trialMatchingChapterType == TrialMatchingChapterType.DETAILED_ALL_TRIALS) {
            addDetailedTrialMatching(document)
        }
    }

    private fun addTrialMatchingResults(document: Document) {
        val table = Tables.createSingleColWithWidth(contentWidth())
        TableGeneratorFunctions.addGenerators(createTrialTableGenerators(), table, overrideTitleFormatToSubtitle = false)
        document.add(table)
    }

    fun createTrialTableGenerators(): List<TableGenerator> {
        val requestingSource = TrialSource.fromDescription(configuration.hospitalOfReference)

        val includeLocalTrialGenerators = configuration.trialMatchingChapterType == TrialMatchingChapterType.STANDARD_ALL_TRIALS ||
                configuration.trialMatchingChapterType == TrialMatchingChapterType.DETAILED_ALL_TRIALS

        val localTrialGenerators = createLocalTrialTableGenerators(
            trialsProvider.evaluableCohorts(), trialsProvider.nonEvaluableCohorts(), requestingSource
        ).takeIf { includeLocalTrialGenerators } ?: emptyList()

        val includeSpecificExternalGenerators =
            configuration.trialMatchingChapterType == TrialMatchingChapterType.STANDARD_EXTERNAL_TRIALS_ONLY ||
                    configuration.trialMatchingChapterType == TrialMatchingChapterType.DETAILED_ALL_TRIALS

        val externalTrials = trialsProvider.externalTrials()
        val nationalExternalTrialGenerator = EligibleTrialGenerator.nationalOpenCohorts(
            emptyList(),
            externalTrials,
            requestingSource,
            configuration.countryOfReference,
            TrialType.EXTERNAL
        ).takeIf { includeSpecificExternalGenerators }

        val internationalExternalTrialGenerator = EligibleTrialGenerator.internationalExternalOpenCohorts(
            externalTrials,
            requestingSource,
        ).takeIf { includeSpecificExternalGenerators }

        val filteredExternalTrialGenerator = EligibleTrialGenerator.filteredExternalTrials(externalTrials, configuration.countryOfReference)

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
        val (ignoredCohorts, nonIgnoredCohorts) = evaluableCohorts.partition { it.ignore }

        val eligibleTrialsClosedCohortsGenerator = EligibleTrialGenerator.closedCohorts(nonIgnoredCohorts, source)
        val ineligibleTrialsGenerator = IneligibleTrialGenerator.evaluableCohorts(nonIgnoredCohorts, source)
        val nonEvaluableOrIgnoredCohortsGenerator = IneligibleTrialGenerator.nonEvaluableOrIgnoredCohorts(
            ignoredCohorts, nonEvaluableCohorts, source
        )

        return listOf(eligibleTrialsClosedCohortsGenerator, ineligibleTrialsGenerator, nonEvaluableOrIgnoredCohortsGenerator)
    }

    private fun addDetailedTrialMatching(document: Document) {
        val (eligible: List<TrialMatch>, nonEligible: List<TrialMatch>) = report.treatmentMatch.trialMatches
            .map(TrialClassification::createForTrialMatch)
            .fold(TrialClassification(), TrialClassification::combine)

        if (eligible.isNotEmpty()) {
            addDetailedTrialMatches(document, eligible, "Potentially eligible open trials & cohorts", true)
        }

        if (nonEligible.isNotEmpty()) {
            if (eligible.isNotEmpty()) {
                document.add(pageBreak())
            }
            addDetailedTrialMatches(document, nonEligible, "Other trials & cohorts", false)
        }
    }

    private fun addDetailedTrialMatches(document: Document, trials: List<TrialMatch>, title: String, trialsAreEligible: Boolean) {
        document.add(Paragraph(title).addStyle(Styles.tableTitleStyle()))
        var addBlank = false
        for (trial in trials) {
            if (addBlank) {
                if (trialsAreEligible) {
                    document.add(pageBreak())
                } else {
                    document.add(blankLine())
                }
            }
            addTrialDetails(document, trial)
            addBlank = true
        }
    }

    private fun addTrialDetails(document: Document, trial: TrialMatch) {
        val displayFailOnly = !trial.isPotentiallyEligible
        document.add(createTrialIdentificationTable(trial.identification, trial.isPotentiallyEligible))
        document.add(blankLine())
        val trialEvaluationPerCriterion = toWorstEvaluationPerReference(trial.evaluations)
        if (hasDisplayableEvaluations(trialEvaluationPerCriterion, displayFailOnly)) {
            document.add(
                Tables.makeWrapping(
                    contentTable = createEvaluationTable(trialEvaluationPerCriterion, displayFailOnly),
                    forceKeepTogether = false
                )
            )
        }
        for (cohort in trial.cohorts) {
            document.add(blankLine())
            document.add(
                createCohortIdentificationTable(
                    trial.identification.trialId,
                    cohort.metadata,
                    cohort.isPotentiallyEligible
                )
            )
            val cohortEvaluationPerCriterion = toWorstEvaluationPerReference(cohort.evaluations)
            if (hasDisplayableEvaluations(cohortEvaluationPerCriterion, displayFailOnly)) {
                document.add(blankLine())
                document.add(
                    Tables.makeWrapping(
                        contentTable = createEvaluationTable(cohortEvaluationPerCriterion, displayFailOnly),
                        forceKeepTogether = false
                    )
                )
            }
        }
    }

    private fun createTrialIdentificationTable(identification: TrialIdentification, isPotentiallyEligible: Boolean): Table {
        val valueWidth = contentWidth() - (KEY_WIDTH + INDENT_WIDTH + 10)
        val table = Tables.createFixedWidthCols(INDENT_WIDTH, KEY_WIDTH, valueWidth).setWidth(contentWidth()).setKeepTogether(true)
        table.addCell(Cells.createSpanningTitle(identification.trialId, table))
        table.addCell(Cells.createEmpty())
        table.addCell(Cells.createKey("Potentially eligible"))
        table.addCell(Cells.createValueYesNo(Formats.yesNoUnknown(isPotentiallyEligible)))
        table.addCell(Cells.createEmpty())
        table.addCell(Cells.createKey("Acronym"))
        table.addCell(Cells.createValue(identification.acronym))
        table.addCell(Cells.createEmpty())
        table.addCell(Cells.createKey("Title"))
        table.addCell(Cells.createValue(identification.title))
        return table
    }

    private fun createCohortIdentificationTable(trialId: String, metadata: CohortMetadata, isPotentiallyEligible: Boolean): Table {
        val valueWidth = contentWidth() - (KEY_WIDTH + INDENT_WIDTH + 10)
        val table = Tables.createFixedWidthCols(INDENT_WIDTH, KEY_WIDTH, valueWidth).setWidth(contentWidth()).setKeepTogether(true)
        table.addCell(Cells.createSpanningTitle(trialId + " - " + metadata.description, table))
        table.addCell(Cells.createEmpty())
        table.addCell(Cells.createKey("Cohort ID"))
        table.addCell(Cells.createValue(metadata.cohortId))
        table.addCell(Cells.createEmpty())
        table.addCell(Cells.createKey("Potentially eligible?"))
        table.addCell(Cells.createValueYesNo(Formats.yesNoUnknown(isPotentiallyEligible)))
        table.addCell(Cells.createEmpty())
        table.addCell(Cells.createKey("Open for inclusion?"))
        table.addCell(Cells.createValue(Formats.yesNoUnknown(metadata.open)))
        table.addCell(Cells.createEmpty())
        table.addCell(Cells.createKey("Has slots available?"))
        table.addCell(Cells.createValue(Formats.yesNoUnknown(metadata.slotsAvailable)))
        if (metadata.ignore) {
            table.addCell(Cells.createEmpty())
            table.addCell(Cells.createKey("Ignored for eligibility?"))
            table.addCell(Cells.createValue(Formats.yesNoUnknown(metadata.ignore)))
        }
        return table
    }

    private fun createEvaluationTable(evaluations: Map<String, Evaluation>, displayFailOnly: Boolean): Table {
        val evaluationWidth = contentWidth() - (INDENT_WIDTH + KEY_WIDTH)
        val table = Tables.createFixedWidthCols(INDENT_WIDTH, KEY_WIDTH, evaluationWidth).setWidth(contentWidth())
        table.addHeaderCell(Cells.createEmpty())
        table.addHeaderCell(Cells.createHeader("Reference"))
        table.addHeaderCell(Cells.createHeader("Evaluation"))

        for (interpretation in EvaluationInterpreter.interpretForDetailedTrialMatching(evaluations, displayFailOnly)) {
            table.addCell(Cells.createEmpty())
            table.addCell(Cells.createContent(interpretation.reference))
            val evalTable = Tables.createSingleColWithWidth(evaluationWidth).setKeepTogether(true)

            val evalCells = interpretation.entriesPerResult.flatMap { (result, entry) ->
                listOf(Cells.createEvaluationResult(result, entry.header)) + entry.messages.map { Cells.create(Paragraph(it)) }
            }
            evalCells.forEach(evalTable::addCell)

            table.addCell(Cells.createContent(evalTable))
        }

        return table
    }

    private fun hasDisplayableEvaluations(
        evaluationsPerCriterion: Map<String, Evaluation>,
        displayFailOnly: Boolean
    ): Boolean {
        return if (displayFailOnly) {
            evaluationsPerCriterion.values.any { it.result == EvaluationResult.FAIL }
        } else {
            evaluationsPerCriterion.isNotEmpty()
        }
    }

    private fun toWorstEvaluationPerReference(evaluations: Map<Eligibility, Evaluation>): Map<String, Evaluation> {
        val worstResultPerCriterion: MutableMap<String, EvaluationResult> = mutableMapOf()
        for ((key, value) in evaluations) {
            for (reference in key.references) {
                val currentWorst = worstResultPerCriterion[reference]
                val evaluation = value.result
                if (currentWorst != null) {
                    val newWorst: EvaluationResult = if (currentWorst.isWorseThan(evaluation)) currentWorst else evaluation
                    worstResultPerCriterion[reference] = newWorst
                } else {
                    worstResultPerCriterion[reference] = evaluation
                }
            }
        }
        val worstEvaluationPerCriterion: MutableMap<String, Evaluation> = mutableMapOf()
        for ((key, evaluation) in evaluations) {
            for (reference in key.references) {
                val worst = worstResultPerCriterion[reference]!!
                val current = worstEvaluationPerCriterion[reference]
                if (current == null) {
                    worstEvaluationPerCriterion[reference] = evaluation.copy(result = worst)
                } else {
                    val recoverable = if (evaluation.result == worst) {
                        current.recoverable && evaluation.recoverable
                    } else current.recoverable
                    worstEvaluationPerCriterion[reference] = current.addMessagesAndEvents(evaluation).copy(recoverable = recoverable)
                }
            }
        }
        return worstEvaluationPerCriterion
    }

    private fun blankLine(): Paragraph {
        return Paragraph(" ")
    }

    private fun pageBreak(): AreaBreak {
        return AreaBreak(AreaBreakType.NEXT_PAGE)
    }

    private data class TrialClassification(
        val eligible: List<TrialMatch> = emptyList(),
        val nonEligible: List<TrialMatch> = emptyList()
    ) {

        fun combine(other: TrialClassification): TrialClassification {
            return TrialClassification(eligible + other.eligible, nonEligible + other.nonEligible)
        }

        companion object {
            fun createForTrialMatch(trialMatch: TrialMatch): TrialClassification {
                return if (isOpenAndPotentiallyEligible(trialMatch)) {
                    TrialClassification(eligible = listOf(trialMatch))
                } else {
                    TrialClassification(nonEligible = listOf(trialMatch))
                }
            }

            private fun isOpenAndPotentiallyEligible(trial: TrialMatch): Boolean {
                if (!trial.isPotentiallyEligible || !trial.identification.open) {
                    return false
                }
                if (trial.cohorts.isEmpty()) {
                    return true
                }
                return trial.cohorts.any { it.isPotentiallyEligible && !it.metadata.ignore && it.metadata.open }
            }
        }
    }
}